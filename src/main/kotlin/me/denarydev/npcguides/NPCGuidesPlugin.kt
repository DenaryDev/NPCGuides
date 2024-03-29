/*
 * Copyright (c) 2024 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides

import me.denarydev.npcguides.command.registerCommands
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

lateinit var plugin: NPCGuidesPlugin
lateinit var logger: Logger

class NPCGuidesPlugin : JavaPlugin() {

    override fun onLoad() {
        plugin = this
        me.denarydev.npcguides.logger = slF4JLogger
    }

    override fun onEnable() {
        dataManager = DataManager()

        if (!server.pluginManager.isPluginEnabled("Citizens")) {
            slF4JLogger.error("Citizens plugin not enabled, disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        guideManager = GuideManager()
        reload()
        ParticlesTask().runTaskTimerAsynchronously(this, main.particlesInterval, main.particlesInterval)
        registerCommands()
        server.pluginManager.registerEvents(PlayerListener(), this)
        server.pluginManager.registerEvents(NPCListener(), this)
    }

    fun reload() {
        loadSettings(dataFolder.toPath())
        dataManager.load(DataConfiguration())
    }

    override fun onDisable() {
        dataManager.shutdown()
    }
}