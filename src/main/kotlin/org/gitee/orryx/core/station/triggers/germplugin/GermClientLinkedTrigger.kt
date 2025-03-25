package org.gitee.orryx.core.station.triggers.germplugin

import com.germ.germplugin.api.event.GermClientLinkedEvent
import org.gitee.orryx.core.station.Plugin
import org.gitee.orryx.core.station.pipe.IPipeTask
import org.gitee.orryx.core.station.triggers.AbstractEventTrigger
import org.gitee.orryx.core.wiki.Trigger
import org.gitee.orryx.core.wiki.TriggerGroup
import org.gitee.orryx.core.wiki.Type
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.ScriptContext

@Plugin("GermPlugin")
object GermClientLinkedTrigger: AbstractEventTrigger<GermClientLinkedEvent>() {

    override val event = "Germ Client Linked"

    override val wiki: Trigger
        get() = Trigger.new(TriggerGroup.GERM_PLUGIN, event)
            .addParm(Type.STRING, "ip", "ip")
            .addParm(Type.STRING, "machineCode", "机器代码")
            .addParm(Type.STRING, "modVersion", "萌芽mod版本")
            .description("玩家进服后萌芽加载完毕")

    override val clazz
        get() = GermClientLinkedEvent::class.java

    override fun onJoin(event: GermClientLinkedEvent, map: Map<String, Any?>): ProxyCommandSender {
        return adaptPlayer(event.player)
    }

    override fun onCheck(pipeTask: IPipeTask, event: GermClientLinkedEvent, map: Map<String, Any?>): Boolean {
        return pipeTask.scriptContext?.sender?.origin == event.player
    }

    override fun onStart(context: ScriptContext, event: GermClientLinkedEvent, map: Map<String, Any?>) {
        super.onStart(context, event, map)
        context["ip"] = event.ip
        context["machineCode"] = event.machineCode
        context["modVersion"] = event.modVersion
    }

}