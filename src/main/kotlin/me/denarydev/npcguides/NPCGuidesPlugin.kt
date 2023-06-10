/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides

import me.denarydev.npcguides.data.DataManager
import me.denarydev.npcguides.data.dataManager
import me.denarydev.npcguides.guide.GuideManager
import me.denarydev.npcguides.guide.guideManager
import me.denarydev.npcguides.listener.NPCListener
import me.denarydev.npcguides.listener.PlayerListener
import me.denarydev.npcguides.settings.DataConfiguration
import me.denarydev.npcguides.settings.loadSettings
import me.denarydev.npcguides.settings.main
import me.denarydev.npcguides.task.ParticlesTask
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger

lateinit var PLUGIN: NPCGuidesPlugin
lateinit var LOGGER: Logger

class NPCGuidesPlugin : JavaPlugin() {

    private lateinit var particlesTask: ParticlesTask

    override fun onLoad() {
        PLUGIN = this
        LOGGER = slF4JLogger
    }

    override fun onEnable() {
        dataManager = DataManager()
        guideManager = GuideManager()
        particlesTask = ParticlesTask()
        reload(true)
        server.pluginManager.registerEvents(PlayerListener(), this)
        server.pluginManager.registerEvents(NPCListener(), this)
    }

    fun reload(startup: Boolean = false) {
        if (!startup) {
            particlesTask.cancel()
            dataManager.shutdown()
        }
        loadSettings(dataFolder.toPath())
        dataManager.loadDatabase(DataConfiguration())
        particlesTask.runTaskTimerAsynchronously(this, main.particlesInterval, main.particlesInterval)
    }

    override fun onDisable() {
        dataManager.shutdown()
    }
}