/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides.command

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import me.rafaelka.npcguides.BUILD_TIME
import me.rafaelka.npcguides.GIT_BRANCH
import me.rafaelka.npcguides.GIT_COMMIT
import me.rafaelka.npcguides.VERSION
import me.rafaelka.npcguides.data.dataManager
import me.rafaelka.npcguides.guide.guideManager
import me.rafaelka.npcguides.plugin
import me.rafaelka.npcguides.settings.messages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.commands.arguments.GameProfileArgument.gameProfile
import net.minecraft.commands.arguments.GameProfileArgument.getGameProfiles
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import java.text.SimpleDateFormat
import java.util.Date

fun registerCommands() {
    val dispatcher = MinecraftServer.getServer().commands.dispatcher

    val cmd = dispatcher.register(guidesCommand())
    dispatcher.register(literal<CommandSourceStack?>("guides").redirect(cmd))
}

private fun guidesCommand(): LiteralArgumentBuilder<CommandSourceStack> {
    return literal<CommandSourceStack?>("npcguides")
        .then(
            literal<CommandSourceStack?>("reload")
                .requires {
                    it.hasPermission(2, "npcguides.reload")
                }
                .executes { ctx ->
                    plugin.reload()
                    ctx.source.bukkitSender.sendRichMessage(messages.commands.reload.reloaded)
                    1
                }
        )
        .then(
            literal<CommandSourceStack?>("about")
                .requires {
                    it.hasPermission(2, "npcguides.about")
                }
                .executes { ctx ->
                    val platformInfo = Component.text(Bukkit.getName()).hoverEvent(
                        HoverEvent.showText(
                            Component.text(Bukkit.getVersion(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                        )
                    )
                    ctx.source.bukkitSender.sendRichMessage(
                        messages.commands.about.success,
                        Placeholder.unparsed("version", VERSION),
                        Placeholder.component("platform", platformInfo),
                        Placeholder.unparsed("author", "RafaelkaUwU (aka DenaryDev)"),
                        Placeholder.unparsed("build_time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss XX").format(Date(BUILD_TIME.toLong()))),
                        Placeholder.unparsed("commit", GIT_COMMIT), Placeholder.unparsed("branch", GIT_BRANCH)
                    )
                    1
                }
        )
        .then(
            literal<CommandSourceStack?>("reset")
                .requires {
                    it.isPlayer && it.hasPermission(2, "npcguides.reset")
                }
                .executes { ctx ->
                    reset(ctx.source)
                    1
                }
                .then(
                    argument<CommandSourceStack?, GameProfileArgument.Result?>("target", gameProfile())
                        .requires {
                            it.hasPermission(2, "npcguides.reset.other")
                        }
                        .executes { ctx ->
                            getGameProfiles(ctx, "target").forEach {
                                reset(ctx.source, MinecraftServer.getServer().playerList.getPlayer(it.id)!!)
                            }
                            1
                        }
                        .then(
                            argument<CommandSourceStack?, String?>("guide", word())
                                .requires {
                                    it.hasPermission(2, "npcguides.reset.other")
                                }
                                .executes { ctx ->
                                    val arg = getString(ctx, "guide")
                                    if (guideManager.hasGuide(arg)) {
                                        getGameProfiles(ctx, "target").forEach {
                                            reset(ctx.source, MinecraftServer.getServer().playerList.getPlayer(it.id)!!, arg)
                                        }
                                    } else {
                                        ctx.source.bukkitSender.sendRichMessage(messages.errors.guideNotFound, Placeholder.unparsed("arg", arg))
                                    }
                                    1
                                }
                        )
                )
        )
}

private fun reset(sender: CommandSourceStack, target: ServerPlayer = sender.player!!, guideId: String? = null) {
    if (dataManager.exists(target.uuid)) {
        if (guideId != null) {
            dataManager.resetPlayerGuide(target.uuid, guideId)
        } else {
            dataManager.resetPlayer(target.uuid)
        }
        sender.bukkitSender.sendRichMessage(messages.commands.reset.sender, Placeholder.unparsed("name", target.displayName))
        target.bukkitEntity.sendRichMessage(messages.commands.reset.target)
    } else {
        sender.bukkitSender.sendRichMessage(messages.errors.noData)
    }
}