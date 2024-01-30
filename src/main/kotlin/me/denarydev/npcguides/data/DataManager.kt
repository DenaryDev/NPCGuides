/*
 * Copyright (c) 2024 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.data

import me.denarydev.crystal.db.DatabaseManager
import me.denarydev.crystal.db.connection.ConnectionFactory
import me.denarydev.crystal.db.settings.ConnectionSettings
import me.denarydev.npcguides.logger
import me.denarydev.npcguides.plugin
import me.denarydev.npcguides.utils.debug
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

lateinit var dataManager: DataManager

class DataManager {
    private val table = "guides_users"
    private val users = mutableMapOf<UUID, MutableMap<String, Int>>()
    private val databaseManager: DatabaseManager = DatabaseManager()
    private lateinit var connectionFactory: ConnectionFactory

    fun load(connectionSettings: ConnectionSettings) {
        databaseManager.initialize(connectionSettings)
        this.connectionFactory = databaseManager.connectionFactory()
        Bukkit.getAsyncScheduler().runNow(plugin) { _ ->
            connectionFactory.connect {
                val statement = it.prepareStatement(
                    """
                    create table if not exists `$table` (
                        `uuid` varchar(36) not null,
                        `name` varchar(16) not null,
                        `guides` text
                    );
                """.trimIndent()
                )
                statement.executeUpdate()
            }
        }
    }

    fun shutdown() {
        databaseManager.shutdown()
    }

    fun loadPlayer(uuid: UUID, name: String) {
        Bukkit.getAsyncScheduler().runNow(plugin) { _ ->
            users[uuid] = talksFromDatabase(uuid, name)
        }
    }

    fun unloadPlayer(player: Player) {
        val talks = users.remove(player.uniqueId) ?: return
        Bukkit.getAsyncScheduler().runNow(plugin) { _ ->
            savePlayer(player.uniqueId, talks)
        }
    }

    fun playerTalks(uuid: UUID): Map<String, Int> {
        return users.getOrDefault(uuid, mutableMapOf())
    }

    fun addTalk(uuid: UUID, guideId: String) {
        if (users.contains(uuid)) {
            val talks = users[uuid]!!
            val current = talks.getOrDefault(guideId, 0)
            talks[guideId] = (current + 1)
            Bukkit.getAsyncScheduler().runNow(plugin) { _ ->
                savePlayer(uuid, talks)
            }
        }
    }

    fun resetPlayerGuide(uuid: UUID, guideId: String) {
        val talks = users.remove(uuid) ?: return
        talks.remove(guideId)
        users[uuid] = talks
        savePlayer(uuid, talks)
    }

    fun resetPlayer(uuid: UUID) {
        if (primaryThread()) return
        connectionFactory.connect {
            val statement = it.prepareStatement("update `$table` set `guides` = null where `uuid` = '$uuid'")
            statement.executeUpdate()
            users[uuid] = mutableMapOf()
        }
    }

    fun exists(uuid: UUID): Boolean {
        if (primaryThread()) return false
        debug("Checking if player $uuid exists in database...")
        connectionFactory.connection().use {
            val statement = it.prepareStatement("select `name` from `$table` where `uuid` = '$uuid';")
            val result = statement.executeQuery()
            return result.next()
        }
    }

    private fun talksFromDatabase(uuid: UUID, name: String): MutableMap<String, Int> {
        if (primaryThread()) return mutableMapOf()
        debug("Getting $name talks from database...")
        connectionFactory.connection().use {
            if (exists(uuid)) {
                val statement = it.prepareStatement("select `guides` from `$table` where `uuid` = '$uuid';")
                val result = statement.executeQuery()
                if (result.next()) {
                    val s = result.getString("guides")
                    if (s != null) {
                        val map = mutableMapOf<String, Int>()
                        if (s.contains("-")) {
                            if (s.contains(";")) {
                                val talks = s.split(";")
                                for (talk in talks) {
                                    val spl = talk.split("-")
                                    if (spl.size == 2) map[spl[0]] = spl[1].toInt()
                                }
                            } else {
                                val spl = s.split("-")
                                if (spl.size == 2) map[spl[0]] = spl[1].toInt()
                            }
                        }
                        debug("Found ${map.size} talks")
                        return map
                    }
                }
            } else {
                debug("Player $name not found in database, creating...")
                val statement = it.prepareStatement("insert into `$table` (`uuid`, `name`) values ('$uuid', '$name');")
                statement.executeUpdate()
            }
            return mutableMapOf()
        }
    }

    private fun savePlayer(uuid: UUID, talks: Map<String, Int>) {
        if (primaryThread()) return
        connectionFactory.connect {
            val statement = it.prepareStatement("update `$table` set `guides` = ${talksToString(talks)} where `uuid` = '${uuid}'")
            statement.executeUpdate()
        }
    }

    private fun talksToString(talks: Map<String, Int>): String? {
        if (talks.isEmpty()) return null
        val builder = StringBuilder()
        talks.forEach { (guideId, amount) ->
            if (builder.isNotEmpty()) builder.append(";")
            builder.append(guideId).append("-").append(amount)
        }
        return "'$builder'"
    }

    private fun primaryThread(): Boolean {
        if (Bukkit.isPrimaryThread()) {
            logger.error("Database access from main thread not allowed")
            return true
        }
        return false
    }
}