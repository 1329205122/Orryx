package org.gitee.orryx.core.ui.dragoncore

import eos.moe.dragoncore.network.PacketSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.gitee.orryx.core.key.BindKeyLoaderManager
import org.gitee.orryx.core.profile.PlayerProfileManager.orryxProfile
import org.gitee.orryx.core.reload.Reload
import org.gitee.orryx.core.skill.SkillLevelResult
import org.gitee.orryx.core.ui.AbstractSkillUI
import org.gitee.orryx.core.ui.IUIManager
import org.gitee.orryx.utils.*
import taboolib.common.platform.function.getDataFolder
import taboolib.platform.util.onlinePlayers
import java.io.File

class DragonCoreSkillUI(override val viewer: Player, override val owner: Player): AbstractSkillUI(viewer, owner) {

    companion object {

        internal lateinit var skillUIConfiguration: YamlConfiguration

        @Reload(2)
        private fun reload() {
            if (IUIManager.INSTANCE !is DragonCoreUIManager) return
            skillUIConfiguration = YamlConfiguration.loadConfiguration(File(getDataFolder(), "ui/dragoncore/OrryxSkillUI.yml"))
            onlinePlayers.forEach {
                PacketSender.sendYaml(it, "gui/OrryxSkillUI.yml", skillUIConfiguration)
            }
        }

        fun update(viewer: Player, owner: Player) {
            val job = owner.job() ?: return
            val bindSkills = job.getBindSkills()
            val keys = bindSkills.keys.sortedBy { it.sort }
            val skills = owner.getSkills()

            PacketSender.sendSyncPlaceholder(viewer, mapOf(
                "Orryx_owner" to owner.uniqueId.toString(),
                "Orryx_job" to job.job.name,
                "Orryx_point" to owner.orryxProfile().point.toString(),
                "Orryx_group" to job.group,
                "Orryx_bind_keys" to keys.joinToString("<br>") { it.key },
                "Orryx_bind_skills" to keys.joinToString("<br>") { bindSkills[it]?.key ?: "none" },
                "Orryx_skills" to skills.joinToString("<br>") { it.key },
                "Orryx_skills_name" to skills.joinToString("<br>") { it.skill.name },
                "Orryx_skills_level" to skills.joinToString("<br>") { it.level.toString() },
                "Orryx_skills_maxLevel" to skills.joinToString("<br>") { it.skill.maxLevel.toString() },
                "Orryx_skills_locked" to skills.joinToString("<br>") { it.locked.toString() },
                "Orryx_skills_point" to skills.joinToString("<br>") { it.upgradePointCheck(it.level, it.level+1).first.toString() }
            ))
        }

        fun sendDescription(viewer: Player, owner: Player, skill: String) {
            val playerSkill = owner.getSkill(skill) ?: return
            PacketSender.sendSyncPlaceholder(viewer, mapOf(
                "Orryx_description" to playerSkill.getDescriptionComparison().joinToString("\n")
            ))
        }

        fun bindSkill(viewer: Player, owner: Player, group: String, bindKey: String, skill: String) {
            if (viewer == owner || viewer.isOp) {
                owner.job { job ->
                    BindKeyLoaderManager.getGroup(group)?.let { group ->
                        BindKeyLoaderManager.getBindKey(bindKey)?.let { bindKey ->
                            owner.getSkill(skill)?.let { skill ->
                                job.setBindKey(skill, group, bindKey).thenRun {
                                    update(viewer, owner)
                                    IUIManager.INSTANCE.getSkillHUD(viewer)?.apply {
                                        update()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun unBindSkill(viewer: Player, owner: Player, group: String, skill: String) {
            if (viewer == owner || viewer.isOp) {
                owner.job { job ->
                    BindKeyLoaderManager.getGroup(group)?.let { group ->
                        owner.getSkill(skill)?.let { skill ->
                            job.unBindKey(skill, group).thenRun {
                                update(viewer, owner)
                                IUIManager.INSTANCE.getSkillHUD(viewer)?.apply {
                                    update()
                                }
                            }
                        }
                    }
                }
            }
        }

        fun upgrade(viewer: Player, owner: Player, skill: String) {
            if (viewer == owner || viewer.isOp) {
                owner.skill(skill) {
                    it.up().thenAccept { result ->
                        if (result == SkillLevelResult.SUCCESS) {
                            update(viewer, owner)
                            sendDescription(viewer, owner, skill)
                        }
                    }
                }
            }
        }

    }

    override fun open() {
        PacketSender.sendOpenGui(viewer, "OrryxSkillUI")
        update()
    }

    override fun update() {
        Companion.update(viewer, owner)
    }

}