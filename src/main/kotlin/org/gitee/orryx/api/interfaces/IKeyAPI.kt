package org.gitee.orryx.api.interfaces

import org.bukkit.entity.Player
import org.gitee.orryx.core.key.IBindKey
import org.gitee.orryx.core.key.IGroup
import org.gitee.orryx.core.profile.IPlayerKeySetting
import org.gitee.orryx.core.skill.IPlayerSkill

interface IKeyAPI {

    /**
     * 获取通用按键设定器
     * @return 按键设置
     * */
    val keySetting: IPlayerKeySetting

    /**
     * 注册通用按键设定器
     * @param keySetting 通用按键设定器
     * */
    fun registerKeySetting(keySetting: IPlayerKeySetting)

    /**
     * 绑定技能到组的按键上
     * @param skill 技能
     * @param group 组
     * @param bindKey 按键
     * @return 是否设置成功
     * */
    fun bindSkillKeyOfGroup(skill: IPlayerSkill, group: IGroup, bindKey: IBindKey): Boolean

    /**
     * 绑定技能到组的按键上
     * @param player 玩家
     * @param skill 技能键名
     * @param group 组键名
     * @param bindKey 按键键名
     * @return 是否设置成功
     * */
    fun bindSkillKeyOfGroup(player: Player, job: String, skill: String, group: String, bindKey: String): Boolean

    /**
     * 获得技能组
     * @param key 技能组名
     * */
    fun getGroup(key: String): IGroup

    /**
     * 获得技能绑定键
     * @param key 绑定键名
     * */
    fun getBindKey(key: String): IBindKey

}