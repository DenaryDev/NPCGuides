/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.command

import me.denarydev.npcguides.PLUGIN
import me.denarydev.npcguides.data.dataManager
import me.denarydev.npcguides.guide.guideManager
import me.denarydev.npcguides.settings.messages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val mm = MiniMessage.miniMessage()

class GuidesCommand : Command("guides") {

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("npcguides.reload")
            && !sender.hasPermission("npcguides.reset")
        //&& !sender.hasPermission("npcguides.info")
        ) {
            sender.sendRichMessage(messages.errors.noPerm)
            return true
        }
        if (args.isEmpty()) {
            sendHelp(sender, label)
            return true
        }

        when (args[0]) {
            "reload" -> {
                if (sender.hasPermission("npcguides.reload")) {
                    PLUGIN.reload()
                    sender.sendRichMessage(messages.commands.reload.reloaded)
                } else {
                    sender.sendRichMessage(messages.errors.noPerm)
                }
            }

            "reset" -> {
                if (sender.hasPermission("npcguides.reset")) {
                    if (args.size == 1) {
                        if (sender is Player) {
                            reset(sender, sender)
                        } else {
                            sender.sendRichMessage(messages.errors.playersOnly)
                        }
                    } else {
                        val target = Bukkit.getPlayer(args[1])
                        if (target != null) {
                            if (args.size == 2) {
                                reset(sender, target)
                            } else if (args.size == 3) {
                                if (guideManager.hasGuide(args[2])) {
                                    reset(sender, target, args[2])
                                } else {
                                    sender.sendMessage(mm.deserialize(messages.errors.guideNotFound, Placeholder.unparsed("arg", args[2])))
                                }
                            } else {
                                sender.sendRichMessage(messages.commands.reset.usage)
                            }
                        } else {
                            sender.sendMessage(mm.deserialize(messages.errors.notFound, Placeholder.unparsed("arg", args[1])))
                        }
                    }
                } else {
                    sender.sendRichMessage(messages.errors.noPerm)
                }
            }

            //"info" -> {
            //    sender.sendRichMessage("Ещё не сделано! <pink>UwU")
            //}

            else -> {
                sender.sendMessage(mm.deserialize(messages.commands.guides.usage, Placeholder.unparsed("label", label)))
            }
        }

        return true
    }

    private fun reset(sender: CommandSender, target: Player, guideId: String? = null) {
        if (dataManager.exists(target.uniqueId)) {
            if (guideId != null) {
                dataManager.resetPlayerGuide(target.uniqueId, guideId)
            } else {
                dataManager.resetPlayer(target.uniqueId)
            }
            sender.sendMessage(mm.deserialize(messages.commands.reset.sender, Placeholder.unparsed("name", target.name)))
            target.sendRichMessage(messages.commands.reset.target)
        } else {
            sender.sendRichMessage(messages.errors.noData)
        }
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        val aliases = mutableListOf<String>()

        if (args.size == 1) {
            val arg0 = args[0]
            if (arg0.isEmpty()) {
                if (sender.hasPermission("npcguides.reload")) aliases.add("reload")
                if (sender.hasPermission("npcguides.reset")) aliases.add("reset")
            } else {
                if ("reload".contains(arg0) && sender.hasPermission("npcguides.reload")) aliases.add("reload")
                if ("reset".contains(arg0) && sender.hasPermission("npcguides.reset")) aliases.add("reset")
            }
        } else if (args.size == 2) {
            val arg0 = args[0]
            val arg1 = args[1]
            if (arg1.isEmpty()) {
                if (arg0 == "reset" && sender.hasPermission("npcguides.reset")) {
                    Bukkit.getOnlinePlayers().forEach {
                        aliases.add(it.name)
                    }
                }
            } else {
                if (arg0 == "reset" && sender.hasPermission("npcguides.reset")) {
                    Bukkit.getOnlinePlayers().forEach {
                        if (it.name.lowercase().contains(arg1)) aliases.add(it.name)
                    }
                }
            }
        }

        return aliases
    }

    private fun sendHelp(sender: CommandSender, label: String) {
        val result = mutableSetOf(mm.deserialize(messages.commands.help.header))
        arrayOf("reload", "reset" /*"info"*/).forEach {
            if (sender.hasPermission("npcguides.$it")) {
                result.add(
                    mm.deserialize(
                        messages.commands.help.entry,
                        Placeholder.unparsed("label", label),
                        Placeholder.unparsed("args", args(it)),
                        Placeholder.component("info", info(it))
                    )
                )
            }
        }
        if (result.size == 1)
            sender.sendRichMessage(messages.errors.noPerm)
        else
            result.forEach(sender::sendMessage)
    }

    private fun args(arg: String): String {
        return when (arg) {
            "reload" -> "reload"
            "reset" -> "reset [player|all]"
            //"info" -> "info [player]"
            else -> "404 Not Found .-."
        }
    }

    private fun info(arg: String): Component {
        return mm.deserialize(
            when (arg) {
                "reload" -> messages.commands.reload.info
                "reset" -> messages.commands.reset.info
                //"info" -> messages.commands.info.info
                else -> "404 Not Found .-."
            }
        )
    }
}
