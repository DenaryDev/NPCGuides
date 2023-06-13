/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.guide

import me.denarydev.npcguides.PLUGIN
import me.denarydev.npcguides.data.dataManager
import me.denarydev.npcguides.settings.Guide
import me.denarydev.npcguides.settings.PermissionAction
import me.denarydev.npcguides.settings.guides
import me.denarydev.npcguides.task.ChatTask
import me.denarydev.npcguides.utils.debug
import net.citizensnpcs.api.npc.NPC
import org.bukkit.entity.Player
import java.util.UUID

lateinit var guideManager: GuideManager

class GuideManager {

    private val talkingPlayers = mutableListOf<UUID>()

    fun byId(id: String): Guide? {
        return guides.stream()
            .filter { it.id == id }
            .findFirst().orElse(null)
    }

    fun startTalking(player: Player, npc: NPC) {
        if (talkingPlayers.contains(player.uniqueId)) {
            debug("Player ${player.name} already talking")
            return
        }
        debug("${player.name} start talking with NPC ${npc.name}")
        val guide = byNPC(npc.id) ?: return
        val action = getAction(player, guide)
        if (action.chat.messages.isNotEmpty()) {
            debug("Found chat messages action, sending...")
            ChatTask(player, guide.id, action).runTaskTimerAsynchronously(PLUGIN, 0L, action.chat.interval * 20L)
            talkingPlayers.add(player.uniqueId)
        }
    }

    fun stopTalking(player: Player, guideId: String, permission: String) {
        talkingPlayers.remove(player.uniqueId)
        if (player.hasPermission(permission)) dataManager.addTalk(player.uniqueId, guideId)
    }

    fun getAction(player: Player, guide: Guide): PermissionAction {
        val talks = dataManager.getPlayerTalks(player.uniqueId)[guide.id] ?: 0
        return guide.interactions.stream()
            .filter {
                it.inRange(talks + 1)
            }
            .map {
                (if (player.hasPermission(it.permission)) it.hasPermission else it.noPermission).permission(it.permission)
            }
            .findFirst().orElse(guide.default)
    }

    private fun byNPC(npcId: Int): Guide? {
        return guides.stream()
            .filter { it.npcId == npcId }
            .findFirst().orElse(null)
    }
}
