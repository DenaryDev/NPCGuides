/*
 * Copyright (c) 2024 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.guide

import me.denarydev.npcguides.data.dataManager
import me.denarydev.npcguides.plugin
import me.denarydev.npcguides.settings.Guide
import me.denarydev.npcguides.settings.PermissionAction
import me.denarydev.npcguides.settings.guides
import me.denarydev.npcguides.task.ChatTask
import me.denarydev.npcguides.utils.debug
import net.citizensnpcs.api.npc.NPC
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

lateinit var guideManager: GuideManager

class GuideManager {

    private val talkingPlayers = mutableMapOf<UUID, BukkitTask>()

    fun hasGuide(id: String): Boolean {
        return byId(id) != null
    }

    fun startTalking(player: Player, npc: NPC) {
        if (talkingPlayers.contains(player.uniqueId)) {
            debug("Player ${player.name} already talking")
            return
        }
        val guide = byNPC(npc.id) ?: return
        debug("${player.name} start talking with NPC ${npc.name}")
        val action = getAction(player, guide)
        if (action.chat.messages.isNotEmpty()) {
            debug("Found chat messages action, sending...")
            val task = ChatTask(player, guide.id, action).runTaskTimerAsynchronously(plugin, 0L, action.chat.interval * 20L)
            talkingPlayers[player.uniqueId] = task
        }
    }

    fun forceStopTalking(player: Player) {
        talkingPlayers[player.uniqueId]?.cancel()
    }

    fun stopTalking(player: Player, guideId: String, permission: String?) {
        talkingPlayers.remove(player.uniqueId)
        if (permission != null && player.hasPermission(permission)) dataManager.addTalk(player.uniqueId, guideId)
    }

    fun getAction(player: Player, guide: Guide): PermissionAction {
        val talks = dataManager.playerTalks(player.uniqueId)[guide.id] ?: 0
        return guide.interactions.stream()
            .filter {
                it.inRange(talks + 1)
            }
            .map {
                if (player.hasPermission(it.permission)) it.hasPermission.permission(it.permission) else it.noPermission
            }
            .findFirst().orElse(guide.default)
    }

    private fun byId(id: String): Guide? {
        return guides.stream()
            .filter { it.id == id }
            .findFirst().orElse(null)
    }

    private fun byNPC(npcId: Int): Guide? {
        return guides.stream()
            .filter { it.npcId == npcId }
            .findFirst().orElse(null)
    }
}