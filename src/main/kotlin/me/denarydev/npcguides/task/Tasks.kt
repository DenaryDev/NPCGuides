/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.task

import me.denarydev.npcguides.PLUGIN
import me.denarydev.npcguides.guide.guideManager
import me.denarydev.npcguides.settings.PermissionAction
import me.denarydev.npcguides.settings.guides
import me.denarydev.npcguides.utils.applyPlaceholders
import me.denarydev.npcguides.utils.debug
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
class ParticlesTask : BukkitRunnable() {
    override fun run() {
        val players = Bukkit.getOnlinePlayers()
        if (players.isEmpty() || guides.isEmpty()) return

        for (player in players) {
            for (guide in guides) {
                val npc = CitizensAPI.getNPCRegistry().getById(guide.npcId) ?: continue
                val action = guideManager.getAction(player, guide)
                if (action.particle.count > 0) ParticleTask(player, npc, action.particle).runTask(PLUGIN)
            }
        }
    }

    private data class ParticleTask(val player: Player, val npc: NPC, val action: PermissionAction.ParticleAction) : BukkitRunnable() {
        override fun run() {
            val nearby = npc.storedLocation.getNearbyPlayers(25.0)
            if (nearby.contains(player))
                player.spawnParticle(action.type, npc.storedLocation.clone().add(0.0, npc.entity.height + action.height, 0.0), action.count, 0.05, 0.02, 0.05)
        }
    }
}

@Internal
data class ChatTask(val player: Player, val guideId: String, val action: PermissionAction) : BukkitRunnable() {
    private var lastIndex: Int = 0

    override fun run() {
        if (lastIndex < action.chat.messages.size) {
            val message = action.chat.messages[lastIndex]
            debug("Sending message to ${player.name}")
            player.sendRichMessage(applyPlaceholders(player, message))
            if (action.chat.sound) {
                player.playSound(player.location, action.sound.type, action.sound.volume, action.sound.pitch)
            }
            lastIndex++
        } else {
            cancel()
            if (action.execute.actions.isNotEmpty()) {
                debug("Found execute action, running...")
                ActionsTask(player, guideId, action).runTask(PLUGIN)
            } else {
                guideManager.stopTalking(player, guideId, action.permission)
            }
        }
    }
}

@Internal
data class ActionsTask(val player: Player, val guideId: String, val action: PermissionAction) : BukkitRunnable() {
    override fun run() {
        action.execute.actions.forEach {
            debug("Executing action $it for ${player.name}")
            executeAction(player, it)
            if (action.execute.sound) {
                player.playSound(player.location, action.sound.type, action.sound.volume, action.sound.pitch)
            }
        }
        guideManager.stopTalking(player, guideId, action.permission)
    }

    private fun executeAction(player: Player, action: String) {
        val spl = action.split(";")
        if (spl.size == 2) {
            val ctx = applyPlaceholders(player, spl[1])
            when (spl[0]) {
                "player" -> {
                    Bukkit.dispatchCommand(player, ctx)
                }

                "console" -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ctx)
                }

                "message" -> {
                    player.sendRichMessage(ctx)
                }
            }
        }
    }
}