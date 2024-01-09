/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides.utils

import me.clip.placeholderapi.PlaceholderAPI
import me.rafaelka.npcguides.logger
import me.rafaelka.npcguides.settings.main
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun debug(s: String) {
    if (main.debug) logger.info("[DEBUG]: $s")
}

fun applyPlaceholders(player: Player, text: String): String {
    var applied = text.replace("{player}", player.name)
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
        applied = PlaceholderAPI.setPlaceholders(player, applied)
    }
    return applied
}