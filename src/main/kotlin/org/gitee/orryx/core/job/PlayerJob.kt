package org.gitee.orryx.core.job

import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.gitee.orryx.api.OrryxAPI.saveScope
import org.gitee.orryx.api.events.player.OrryxPlayerChangeGroupEvents
import org.gitee.orryx.api.events.player.job.OrryxPlayerJobClearEvents
import org.gitee.orryx.api.events.player.job.OrryxPlayerJobExperienceEvents
import org.gitee.orryx.api.events.player.job.OrryxPlayerJobLevelEvents
import org.gitee.orryx.api.events.player.skill.OrryxPlayerSkillBindKeyEvent
import org.gitee.orryx.api.events.player.skill.OrryxPlayerSkillUnBindKeyEvent
import org.gitee.orryx.core.GameManager
import org.gitee.orryx.core.experience.ExperienceLoaderManager
import org.gitee.orryx.core.experience.IExperience
import org.gitee.orryx.core.job.ExperienceResult.*
import org.gitee.orryx.core.key.BindKeyLoaderManager
import org.gitee.orryx.core.key.IBindKey
import org.gitee.orryx.core.key.IGroup
import org.gitee.orryx.core.skill.IPlayerSkill
import org.gitee.orryx.dao.cache.ICacheManager
import org.gitee.orryx.dao.pojo.PlayerJobPO
import org.gitee.orryx.dao.storage.IStorageManager
import org.gitee.orryx.utils.*
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.util.unsafeLazy
import taboolib.common5.cdouble
import taboolib.module.kether.orNull
import java.util.concurrent.CompletableFuture

class PlayerJob(
    override val player: Player,
    override val key: String,
    private var privateExperience: Int,
    private var privateGroup: String = DEFAULT,
    private val privateBindKeyOfGroup: MutableMap<IGroup, MutableMap<IBindKey, String?>>
): IPlayerJob {

    override val job: IJob by unsafeLazy { JobLoaderManager.getJobLoader(key)!! }

    override val bindKeyOfGroup: Map<IGroup, Map<IBindKey, String?>>
        get() = privateBindKeyOfGroup

    override val experience: Int
        get() = privateExperience

    override val group: String
        get() = privateGroup

    override val experienceOfLevel: Int
        get() = getExperience().getLessExp(player, experience)

    override val level: Int
        get() = getExperience().getLevel(player, experience)

    override val maxExperienceOfLevel: Int
        get() = getExperience().getExperienceOfLevel(player, experience)

    private fun createDaoData(): PlayerJobPO {
        return PlayerJobPO(player.uniqueId, key, experience, group, bindKeyOfGroupToMap(bindKeyOfGroup))
    }

    override fun getExperience(): IExperience {
        return ExperienceLoaderManager.getExperience(job.experience) ?: error("职业${key}的经验计算器${job.experience}找不到")
    }

    override fun getMaxMana(): Double {
        return player.eval(job.maxManaActions, mapOf("level" to level)).orNull().cdouble
    }

    override fun getReginMana(): Double {
        return player.eval(job.regainManaActions, mapOf("level" to level)).orNull().cdouble
    }

    override fun giveExperience(experience: Int): CompletableFuture<ExperienceResult> {
        if (experience < 0) return takeExperience(-experience)
        val event = OrryxPlayerJobExperienceEvents.Up(player, this, experience)
        val future = CompletableFuture<ExperienceResult>()
        if (event.call()) {
            val before = level
            privateExperience = (privateExperience + event.upExperience.coerceAtLeast(0)).coerceAtMost(getExperience().maxExp(player))
            save(isPrimaryThread) {
                val changeLevel = level - before
                if (changeLevel > 0) {
                    OrryxPlayerJobLevelEvents.Up(player, this, changeLevel).call()
                }
                future.complete(SUCCESS)
            }
        } else {
            future.complete(CANCELLED)
        }
        return future
    }

    override fun takeExperience(experience: Int): CompletableFuture<ExperienceResult> {
        if (experience < 0) return giveExperience(-experience)
        val event = OrryxPlayerJobExperienceEvents.Down(player, this, experience)
        val future = CompletableFuture<ExperienceResult>()
        if (event.call()) {
            val before = level
            privateExperience = (privateExperience - event.downExperience.coerceAtLeast(0)).coerceAtLeast(0)
            save(isPrimaryThread) {
                val changeLevel = before - level
                if (changeLevel > 0) {
                    OrryxPlayerJobLevelEvents.Down(player, this, changeLevel).call()
                }
                future.complete(SUCCESS)
            }
        } else {
            future.complete(CANCELLED)
        }
        return future
    }

    override fun setExperience(experience: Int): CompletableFuture<ExperienceResult> {
        return when {
            experience > this.experience -> giveExperience(experience - this.experience)
            experience < this.experience -> takeExperience(this.experience - experience)
            else -> CompletableFuture.completedFuture(SAME)
        }
    }

    override fun giveLevel(level: Int): CompletableFuture<LevelResult> {
        if (level < 0) return takeLevel(-level)
        return giveExperience(getExperience().getExperienceFromTo(player, this.level, this.level + level)).thenApply { result ->
            when(result) {
                CANCELLED -> LevelResult.CANCELLED
                SUCCESS -> LevelResult.SUCCESS
                SAME -> LevelResult.SAME
                else -> error("Give Level Result NULL")
            }
        }
    }

    override fun takeLevel(level: Int): CompletableFuture<LevelResult> {
        if (level < 0) return giveLevel(-level)
        return takeExperience(getExperience().getExperienceFromTo(player, this.level - level, this.level)).thenApply { result ->
            when(result) {
                CANCELLED -> LevelResult.CANCELLED
                SUCCESS -> LevelResult.SUCCESS
                SAME -> LevelResult.SAME
                else -> error("Take Level Result NULL")
            }
        }
    }

    override fun setLevel(level: Int): CompletableFuture<LevelResult> {
        return when {
            level < this.level -> takeLevel(this.level - level)
            level > this.level -> giveLevel(level - this.level)
            else -> CompletableFuture.completedFuture(LevelResult.SAME)
        }
    }

    override fun setGroup(group: String): CompletableFuture<Boolean> {
        val iGroup = BindKeyLoaderManager.getGroup(group) ?: return CompletableFuture.completedFuture(false)
        val event = OrryxPlayerChangeGroupEvents.Pre(player, this, iGroup)
        val future = CompletableFuture<Boolean>()
        if (event.call()) {
            privateGroup = event.group.key
            save(isPrimaryThread) {
                OrryxPlayerChangeGroupEvents.Post(player, this, event.group).call()
                future.complete(true)
            }
        } else {
            future.complete(false)
        }
        return future
    }

    override fun setBindKey(skill: IPlayerSkill, group: IGroup, bindKey: IBindKey): CompletableFuture<Boolean> {
        val event = OrryxPlayerSkillBindKeyEvent.Pre(player, skill, group, bindKey)
        val future = CompletableFuture<Boolean>()
        if (event.call()) {
            privateBindKeyOfGroup.getOrPut(event.group) { hashMapOf() }.apply {
                replaceAll { _, u ->
                    if (u == skill.key) {
                        null
                    } else {
                        u
                    }
                }
                set(event.bindKey, skill.key)
            }
            save(isPrimaryThread) {
                OrryxPlayerSkillBindKeyEvent.Post(player, skill, event.group, event.bindKey).call()
                future.complete(true)
            }
        } else {
            future.complete(false)
        }
        return future
    }

    override fun unBindKey(skill: IPlayerSkill, group: IGroup): CompletableFuture<Boolean> {
        val event = OrryxPlayerSkillUnBindKeyEvent.Pre(player, skill, group)
        val future = CompletableFuture<Boolean>()
        if (event.call()) {
            privateBindKeyOfGroup[event.group]?.replaceAll { _, u ->
                if (u == skill.key) {
                    null
                } else {
                    u
                }
            }
            save(isPrimaryThread) {
                OrryxPlayerSkillUnBindKeyEvent.Post(player, skill, event.group).call()
                future.complete(true)
            }
        } else {
            future.complete(false)
        }
        return future
    }

    override fun clear(): CompletableFuture<Boolean> {
        val event = OrryxPlayerJobClearEvents.Pre(player, this)
        val future = CompletableFuture<Boolean>()
        future.thenApply {
            if (it) OrryxPlayerJobClearEvents.Post(player, this).call()
        }
        if (event.call()) {
            privateBindKeyOfGroup.clear()
            privateExperience = 0
            privateGroup = DEFAULT
            var var1 = 0
            val list = job.skills.mapNotNull { player.getSkill(it) }
            list.forEach {
                it.clear().whenComplete { _, _ ->
                    var1++
                    if (var1 > list.size) {
                        future.complete(true)
                    }
                }
            }
            save(isPrimaryThread) {
                var1++
                if (var1 > list.size) {
                    future.complete(true)
                }
            }
        } else {
            future.complete(false)
        }
        return future
    }

    override fun save(async: Boolean, callback: () -> Unit) {
        val data = createDaoData()
        if (async && !GameManager.shutdown) {
            saveScope.launch {
                IStorageManager.INSTANCE.savePlayerJob(player.uniqueId, data)
                ICacheManager.INSTANCE.savePlayerJob(player.uniqueId, data, false)
            }.invokeOnCompletion {
                callback()
            }
        } else {
            IStorageManager.INSTANCE.savePlayerJob(player.uniqueId, data)
            ICacheManager.INSTANCE.savePlayerJob(player.uniqueId, data, false)
            callback()
        }
    }

}