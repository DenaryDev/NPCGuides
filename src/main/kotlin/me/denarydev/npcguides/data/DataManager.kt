/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.data

import me.denarydev.crystal.db.AbstractDataManager
import me.denarydev.npcguides.utils.debug
import org.bukkit.entity.Player
import java.util.UUID

lateinit var dataManager: DataManager

class DataManager : AbstractDataManager() {

    private val table = "guides_users"

    private val users = mutableMapOf<UUID, MutableMap<String, Int>>()

    override fun onDatabaseLoad() {
        runAsync {
            databaseConnector.connection.use {
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

    fun createPlayer(player: Player) {
        runAsync {
            users[player.uniqueId] = getPlayerTalks(player)
        }
    }

    fun deletePlayer(player: Player) {
        users.remove(player.uniqueId) ?: return
        savePlayer(player.uniqueId)
    }

    fun getPlayerTalks(uuid: UUID): Map<String, Int> {
        return users.getOrDefault(uuid, mutableMapOf())
    }

    fun addTalk(uuid: UUID, guideId: String) {
        if (users.contains(uuid)) {
            val user = users[uuid]!!
            val current = user.getOrDefault(guideId, 0)
            user[guideId] = (current + 1)
            savePlayer(uuid)
        }
    }

    fun shutdown() {
        shutdownTaskQueue()
        if (databaseConnector != null) databaseConnector.closeConnection()
    }

    fun resetPlayer(uuid: UUID) {
        databaseConnector.connection.use {
            val statement = it.prepareStatement("update `$table` set `guides` = null where `uuid` = '$uuid'")
            statement.executeUpdate()
        }
    }

    fun exists(uuid: UUID): Boolean {
        databaseConnector.connection.use {
            val statement = it.prepareStatement("select `name` from `$table` where `uuid` = '$uuid';")
            val result = statement.executeQuery()
            return result.next()
        }
    }

    private fun getPlayerTalks(player: Player): MutableMap<String, Int> {
        debug("Getting ${player.name} talks from database...")
        databaseConnector.connection.use {
            if (exists(player.uniqueId)) {
                val statement = it.prepareStatement("select `guides` from `$table` where `uuid` = '${player.uniqueId}';")
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
                val statement = it.prepareStatement("insert into `$table` (`uuid`, `name`) values ('${player.uniqueId}', '${player.name}');")
                statement.executeUpdate()
            }
            return mutableMapOf()
        }
    }

    private fun savePlayer(uuid: UUID) {
        databaseConnector.connection.use {
            val statement = it.prepareStatement("update `$table` set `guides` = ${talksAsString(uuid)} where `uuid` = '${uuid}'")
            statement.executeUpdate()
        }
    }

    private fun talksAsString(uuid: UUID): String? {
        val talks = users[uuid] ?: return null
        if (talks.isEmpty()) return null
        val builder = StringBuilder()
        talks.forEach { (guideId, amount) ->
            if (builder.isNotEmpty()) builder.append(";")
            builder.append(guideId).append("-").append(amount)
        }
        return "'$builder'"
    }
}
