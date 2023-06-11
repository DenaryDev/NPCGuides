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
                    if (args.size == 1 && sender is Player) {
                        reset(sender, sender)
                    } else if (args.size == 2) {
                        val target = Bukkit.getPlayer(args[1])
                        if (target != null) {
                            reset(sender, target)
                        } else {
                            sender.sendMessage(mm.deserialize(messages.errors.notFound, Placeholder.unparsed("arg", args[1])))
                        }
                    } else {
                        sender.sendRichMessage(messages.commands.reset.usage)
                    }
                } else {
                    sender.sendRichMessage(messages.errors.noPerm)
                }
            }

            "info" -> {
                sender.sendRichMessage("Ещё не сделано! <pink>UwU")
            }

            else -> {
                sender.sendMessage(mm.deserialize(messages.commands.guides.usage, Placeholder.unparsed("label", label)))
            }
        }

        return true
    }

    private fun reset(sender: CommandSender, target: Player) {
        if (dataManager.exists(target.uniqueId)) {
            dataManager.resetPlayer(target.uniqueId)
            sender.sendMessage(mm.deserialize(messages.commands.reset.sender, Placeholder.unparsed("name", sender.name)))
            target.sendRichMessage(messages.commands.reset.target)
        } else {
            sender.sendRichMessage(messages.errors.noData)
        }
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        return super.tabComplete(sender, alias, args)
    }
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