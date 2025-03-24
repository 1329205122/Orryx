package org.gitee.orryx.core.station.triggers.bukkit

import org.bukkit.event.player.PlayerEvent
import org.gitee.orryx.core.station.pipe.IPipeTask
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer

abstract class AbstractPlayerEventTrigger<E : PlayerEvent>: AbstractEventTrigger<E>() {

    override fun onJoin(event: E, map: Map<String, Any?>): ProxyCommandSender {
        return adaptPlayer(event.player)
    }

    override fun onCheck(pipeTask: IPipeTask, event: E, map: Map<String, Any?>): Boolean {
        return pipeTask.scriptContext?.sender?.origin == event.player
    }

}