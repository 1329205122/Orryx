package org.gitee.orryx.core.selector.presets

import org.gitee.orryx.core.reload.Reload
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object SelectorPresetsLoaderManager {

    @Config("selectors.yml")
    lateinit var selectors: Configuration

    private val selectorPresets by lazy { mutableMapOf<String, PresetsLoader>() }

    internal fun getSelectorPreset(key: String): PresetsLoader? {
        return selectorPresets[key]
    }

    @Reload(weight = 1)
    @Awake(LifeCycle.ENABLE)
    private fun reload() {
        selectorPresets.clear()
        selectors.reload()
        selectors.getKeys(false).forEach { key ->
            selectorPresets[key] = PresetsLoader(arrayOf(key), selectors.getConfigurationSection(key)!!)
        }
        info("&e┣&7Selectors loaded &e${selectorPresets.size} &a√".colored())
    }

}