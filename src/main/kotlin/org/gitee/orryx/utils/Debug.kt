package org.gitee.orryx.utils

import org.gitee.orryx.api.OrryxAPI
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO
import taboolib.module.chat.colored

var debug: Boolean = OrryxAPI.config.getBoolean("Debug")

fun debug(vararg message: Any?) {
    if (debug) PlatformFactory.getService<PlatformIO>().info(*message.map { "&6[debug] $it".colored() }.toTypedArray())
}