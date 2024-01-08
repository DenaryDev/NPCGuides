/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.rafaelka.npcguides.settings

import me.denarydev.crystal.db.AbstractDatabaseConfig
import me.denarydev.crystal.db.DatabaseType
import me.rafaelka.npcguides.logger
import me.rafaelka.npcguides.plugin
import org.bukkit.Bukkit
import org.slf4j.Logger
import java.io.File

class DataConfiguration : AbstractDatabaseConfig() {
    override fun logger(): Logger {
        return logger
    }

    override fun runSyncTask(task: Runnable) {
        Bukkit.getScheduler().runTask(plugin, task)
    }

    override fun databaseType(): DatabaseType {
        return main.database.type
    }

    override fun sqliteStorageFile(): File {
        return File(plugin.dataFolder, "storage.db")
    }

    override fun sqlPoolPrefix(): String {
        return plugin.name
    }

    override fun address(): String {
        return main.database.remote.connection.address
    }

    override fun port(): Int {
        return main.database.remote.connection.port
    }

    override fun database(): String {
        return main.database.remote.connection.database
    }

    override fun username(): String {
        return main.database.remote.credentials.username
    }

    override fun password(): String {
        return main.database.remote.credentials.password
    }

    override fun maxPoolSize(): Short {
        return main.database.remote.settings.maxPoolSize.toShort()
    }

    override fun minimumIdle(): Short {
        return main.database.remote.settings.minimumIdle.toShort()
    }

    override fun maxLifeTime(): Int {
        return main.database.remote.settings.maxLifeTime
    }

    override fun keepAliveTime(): Int {
        return main.database.remote.settings.keepAliveTime
    }

    override fun connectionTimeout(): Int {
        return main.database.remote.settings.connectionTimeout
    }

    override fun properties(): MutableMap<String, String> {
        return main.database.remote.properties
    }
}