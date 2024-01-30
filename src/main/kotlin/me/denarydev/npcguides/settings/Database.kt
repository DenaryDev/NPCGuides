/*
 * Copyright (c) 2024 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.settings

import me.denarydev.crystal.db.DatabaseType
import me.denarydev.crystal.db.settings.FlatfileConnectionSettings
import me.denarydev.crystal.db.settings.HikariConnectionSettings
import me.denarydev.npcguides.logger
import me.denarydev.npcguides.plugin
import org.slf4j.Logger
import java.nio.file.Path

class DataConfiguration : HikariConnectionSettings, FlatfileConnectionSettings {
    override fun pluginName(): String {
        return plugin.name
    }

    override fun databaseType(): DatabaseType {
        return main.database.type
    }

    override fun logger(): Logger {
        return logger
    }

    override fun dataFolder(): Path {
        return plugin.dataFolder.toPath()
    }

    override fun address(): String {
        return main.database.remote.connection.address
    }

    override fun port(): String {
        return main.database.remote.connection.port.toString()
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

    override fun maxPoolSize(): Int {
        return main.database.remote.settings.maxPoolSize
    }

    override fun minimumIdle(): Int {
        return main.database.remote.settings.minimumIdle
    }

    override fun maxLifetime(): Int {
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