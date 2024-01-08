/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides.listener

import me.rafaelka.npcguides.data.dataManager
import me.rafaelka.npcguides.guide.guideManager
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        dataManager.createPlayer(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        dataManager.deletePlayer(event.player)

        guideManager.forceStopTalking(event.player)
    }
}

class NPCListener : Listener {

    @EventHandler
    fun onNPCClick(event: NPCRightClickEvent) {
        val player = event.clicker
        guideManager.startTalking(player, event.npc)
    }
}