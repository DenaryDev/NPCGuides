/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides

import me.rafaelka.npcguides.command.registerCommands
import me.rafaelka.npcguides.data.DataManager
import me.rafaelka.npcguides.data.dataManager
import me.rafaelka.npcguides.guide.GuideManager
import me.rafaelka.npcguides.guide.guideManager
import me.rafaelka.npcguides.listener.NPCListener
import me.rafaelka.npcguides.listener.PlayerListener
import me.rafaelka.npcguides.settings.DataConfiguration
import me.rafaelka.npcguides.settings.loadSettings
import me.rafaelka.npcguides.settings.main
import me.rafaelka.npcguides.task.ParticlesTask
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger

lateinit var plugin: NPCGuidesPlugin
lateinit var logger: Logger

class NPCGuidesPlugin : JavaPlugin() {

    override fun onLoad() {
        plugin = this
        me.rafaelka.npcguides.logger = slF4JLogger
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
        dataManager.loadDatabase(DataConfiguration())
    }

    override fun onDisable() {
        dataManager.shutdown()
    }
}