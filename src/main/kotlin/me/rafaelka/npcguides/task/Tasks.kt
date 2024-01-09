/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides.task

import me.rafaelka.npcguides.guide.guideManager
import me.rafaelka.npcguides.logger
import me.rafaelka.npcguides.plugin
import me.rafaelka.npcguides.settings.PermissionAction
import me.rafaelka.npcguides.settings.guides
import me.rafaelka.npcguides.utils.applyPlaceholders
import me.rafaelka.npcguides.utils.debug
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

internal class ParticlesTask : BukkitRunnable() {
    override fun run() {
        val players = Bukkit.getOnlinePlayers()
        if (players.isEmpty() || guides.isEmpty()) return

        for (player in players) {
            for (guide in guides) {
                val npc = CitizensAPI.getNPCRegistry().getById(guide.npcId) ?: continue
                val action = guideManager.getAction(player, guide)
                if (action.particle.count > 0) ParticleTask(player, npc, action.particle).runTask(plugin)
            }
        }
    }
}

internal data class ParticleTask(val player: Player, val npc: NPC, val action: PermissionAction.ParticleAction) : BukkitRunnable() {
    override fun run() {
        val npcLocation = npc.storedLocation
        if (npcLocation == null) {
            logger.error("Cannot find location of npc ${npc.name} (id: ${npc.id})")
            cancel()
            return
        }
        val nearby = npc.storedLocation.getNearbyPlayers(25.0)
        if (nearby.contains(player))
            player.spawnParticle(action.type, npc.storedLocation.clone().add(0.0, npc.entity.height + action.height, 0.0), action.count, 0.05, 0.02, 0.05)
    }
}

internal data class ChatTask(val player: Player, val guideId: String, val action: PermissionAction) : BukkitRunnable() {
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
                action.execute.actions.forEach {
                    debug("Executing action $it for ${player.name}")
                    executeAction(player, it)
                    if (action.execute.sound) {
                        player.playSound(player.location, action.sound.type, action.sound.volume, action.sound.pitch)
                    }
                }
                guideManager.stopTalking(player, guideId, action.permission)
            } else {
                guideManager.stopTalking(player, guideId, action.permission)
            }
        }
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