/*
 * Copyright (c) 2023 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.settings

import me.denarydev.crystal.config.CrystalConfigs
import me.denarydev.crystal.db.DatabaseType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.nio.file.Path

lateinit var main: MainConfig
lateinit var guides: GuidesConfig
lateinit var messages: MessagesConfig

fun loadSettings(path: Path) {
    main = CrystalConfigs.loadConfig(path.resolve("settings.conf"), MainConfig::class.java, false)
    guides = CrystalConfigs.loadConfig(path.resolve("guides.conf"), GuidesConfig::class.java, false)
    messages = CrystalConfigs.loadConfig(path.resolve("messages.conf"), MessagesConfig::class.java, false)
}

//TODO: Fix comments in configuration

@ConfigSerializable
class MainConfig {
    @Comment("Режим отладки")
    val debug: Boolean = true
    @Comment("Интервал появления частиц над нпс (в тиках)")
    val particlesInterval: Long = 5L
    @Comment("Настройки базы данных")
    val database: Database = Database()

    @ConfigSerializable
    class Database {
        @Comment("Тип базы данных. Доступно: SQLITE, MYSQL")
        val type: DatabaseType = DatabaseType.MYSQL
        @Comment("Настройки удалённого подключения (для MySQL)")
        val remote: Remote = Remote()
    
        @ConfigSerializable
        class Remote {
            @Comment("Настройки адреса бд")
            val connection: Connection = Connection()
            @Comment("Пользователь, используемый для подключения к бд")
            val credentials: Credentials = Credentials()
            @Comment("Основные параметры подключения")
            val settings: Settings = Settings()
            @Comment("Дополнительные параметры, которые вы можете использовать.")
            val properties: MutableMap<String, String> = mutableMapOf(
                "useUnicode" to "true",
                "characterEncoding" to "utf8",
                "useSSL" to "false",
                "verifyServerCertificate" to "false"
            )
        
            @ConfigSerializable
            class Connection {
                @Comment("Адрес удалённого сервера")
                val address: String = "localhost"

                @Comment("Порт удалённого сервера")
                val port: Int = 3306

                @Comment("База данных, в которой будут храниться данные")
                val database: String = "plugintests"
            }

            @ConfigSerializable
            class Credentials {
                @Comment("Имя пользователя")
                val username: String = "whoareyou"

                @Comment("Пароль")
                val password: String = ">ocGxR#g,5yt9j6"
            }

            @ConfigSerializable
            class Settings {
                @Comment(
                    """Максимальное количество одновременных подключений.
Должно быть так же, сколько у вас ядер.
Значение по умолчанию: 6"""
                )
                val maxPoolSize: Short = 6

                @Comment(
                    """Количество соединений, которые всегда должны быть открыты.
Чтобы избежать проблем, установите то же значение, что и maxPoolSize.
Значение по умолчанию: 6"""
                )
                val minimumIdle: Short = 6

                @Comment(
                    """Количество миллисекунд, в течение которых одно соединение должно оставаться открытым.
Значение по умолчанию: 1800000"""
                )
                val maxLifeTime: Int = 1800000

                @Comment(
                    """Установка интервала, в течение которого нужно «пинговать» базу данных. Установите 0, чтобы отключить.
Значение по умолчанию: 0"""
                )
                val keepAliveTime: Int = 0

                @Comment(
                    """Количество секунд, в течение которых мы ждем ответа от базы данных, прежде чем истечет время ожидания.
Значение по умолчанию: 500"""
                )
                val connectionTimeout: Int = 5000
            }
        }
    }
}

@ConfigSerializable
class GuidesConfig(
    val guides: List<Guide> = listOf(Guide())
)

@ConfigSerializable
class MessagesConfig {
    @Comment("Сообщения о различных ошибках")
    val errors: Errors = Errors()
    @Comment("Сообщения для команд")
    val commands: Commands = Commands()

    @ConfigSerializable
    class Errors {
        val notFound: String = "<red>Игрок %argument% не найден."
        val playersOnly: String = "<red>Эту команду могут использовать только игроки."
        val noPerm: String = "<red>У вас недостаточно прав, чтобы использовать эту команду."
    }

    @ConfigSerializable
    class Commands {
        val guides: Guides = Guides()
        val help: Help = Help()
        val reset: Reset = Reset()
        val reload: Reload = Reload()
    
        @ConfigSerializable
        class Guides {
            val usage: String = "<red>Использование: /<label> <help|reload|reset> [player|all]"
        }

        @ConfigSerializable
        class Help {
            val header: String = "<yellow>Помощь по команде /<label>:"
            val entry: String = "<dark_gray><b>*</b> <gold><usage> <gray>- <yellow><info><reset>"
        }

        @ConfigSerializable
        class Reset {
            val info: String = "<i>Удаляет данные игрока или всех игроков"
            val usage: String = "<red>Использование: /<label> reset [player|all]"
            val yourself: String = "<green>Ваши данные были удалены из базы данных, вы можете снова проходить гайды."
            val all: String = "<green>Данные всех игроков удалены из базы данных, они могут снова проходить гайды."
            val player: String = "<green>Данные игрока %player_name% удалены, он может снова проходить гайды."
        }

        @ConfigSerializable
        class Reload {
            val info: String = "<i>Перезагружает конфигурацию плагина"
            val usage: String = "<red>Использование: /<label> reload"
            val reloaded: String = "<green>Конфигурация плагина успешно перезагружена."
        }
    }
}