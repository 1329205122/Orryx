package org.gitee.orryx

import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin

object OrryxPlugin : Plugin() {

    override fun onEnable() {
        Metrics(24289, BukkitPlugin.getInstance().description.version, Platform.BUKKIT)
        info("&e┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
        info("&e┃&a _____                                                  ".colored())
        info("&e┃&a/\\  __`\\                                              ".colored())
        info("&e┃&a\\ \\ \\/\\ \\  _ __   _ __   __  __   __  _            ".colored())
        info("&e┃&a \\ \\ \\ \\ \\/\\`'__\\/\\`'__\\/\\ \\/\\ \\ /\\ \\/'\\".colored())
        info("&e┃&a  \\ \\ \\_\\ \\ \\ \\/ \\ \\ \\/ \\ \\ \\_\\ \\\\/>  </".colored())
        info("&e┃&a   \\ \\_____\\ \\_\\  \\ \\_\\  \\/`____ \\/\\_/\\_\\  ".colored())
        info("&e┃&a    \\/_____/\\/_/   \\/_/   `/___/> \\//\\/_/          ".colored())
        info("&e┃&a                             /\\___/                    ".colored())
        info("&e┃&a                             \\/__/                     ".colored())
        info("&e┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
    }

    override fun onDisable() {
        info("&eOrryx &a卸载".colored())
    }

}