package org.gitee.orryx.core.kether.parameter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.gitee.orryx.core.kether.ScriptManager
import org.gitee.orryx.core.station.stations.IStation
import org.gitee.orryx.core.station.stations.StationLoaderManager
import org.gitee.orryx.core.targets.ITarget
import org.gitee.orryx.utils.toTarget
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.warning
import taboolib.module.kether.orNull

class StationParameter(val stationLoader: String, val sender: ProxyCommandSender): IParameter {

    override var origin: ITarget<*>? = sender.castSafely<Player>()?.toTarget()

    private val lazies by lazy { mutableMapOf<String, Any?>() }

    fun getOriginLocation(): Location? {
        return origin?.location
    }

    fun getStation(): IStation {
        return StationLoaderManager.getStationLoader(stationLoader) ?: error("${stationLoader}未找到")
    }

    override fun getVariable(key: String, lazy: Boolean): Any? {
        fun getAndSetValue(): Any? {
            val value = getStation().variables[key]?.let { ScriptManager.runScript(sender, this, it).orNull() }
            if (value == null) {
                warning("未找到中转站${stationLoader}的变量$key")
                return null
            } else {
                lazies[key] = value
                return value
            }
        }
        return if (lazy) {
            lazies[key] ?: getAndSetValue()
        } else {
            getAndSetValue()
        }
    }

}