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
import me.denarydev.npcguides.LOGGER
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

lateinit var main: MainConfig
lateinit var messages: MessagesConfig

val guides = mutableListOf(Guide())

fun loadSettings(path: Path) {
    main = CrystalConfigs.loadConfig(path.resolve("settings.conf"), MainConfig::class.java, false)
    messages = CrystalConfigs.loadConfig(path.resolve("messages.conf"), MessagesConfig::class.java, false)
    loadGuides(path.resolve("guides"))
}

private fun loadGuides(path: Path) {
    if (!Files.exists(path)) Files.createDirectories(path)
    val files = Files.list(path).use { it.toList() }
    if (files != null && files.isNotEmpty()) {
        files.forEach {
            if (it != null) {
                val config = CrystalConfigs.loadConfig(path.resolve(it.name), Guide::class.java, false)
                guides.add(config.id(it.name.substring(0, it.name.length - 5)))
            }
        }
    } else {
        val default = Guide()
        val loader = CrystalConfigs.hoconLoader(path.resolve("madadm.conf"))
        val node = loader.load()
        node.set(default)
        loader.save(node)
        guides.add(default)
    }
    LOGGER.info("Loaded ${guides.size} guide configs")
}

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

            @Comment(
                """Дополнительные параметры, которые вы можете использовать.
Можно добавить следующие параметры:
  useSSL = false
  verifyServerCertificate = false"""
            )
            val properties: MutableMap<String, String> = mutableMapOf(
                "useUnicode" to "true",
                "characterEncoding" to "utf8"
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
class MessagesConfig {
    @Comment("Сообщения о различных ошибках")
    val errors: Errors = Errors()

    @Comment("Сообщения для команд")
    val commands: Commands = Commands()

    @ConfigSerializable
    class Errors {
        val notFound: String = "<red>Игрок <arg> не найден."
        val playersOnly: String = "<red>Эту команду могут использовать только игроки."
        val noPerm: String = "<red>У вас недостаточно прав, чтобы использовать эту команду."
        val noData: String = "<red>Данные о игроке не найдены"
    }

    @ConfigSerializable
    class Commands {
        val guides: Guides = Guides()
        val help: Help = Help()
        val reset: Reset = Reset()
        val reload: Reload = Reload()
        //val info: Info = Info()

        @ConfigSerializable
        class Guides {
            val usage: String = "<red>Используйте /<label> help для просмотра списка доступных команд"
        }

        @ConfigSerializable
        class Help {
            val header: String = "<rainbow><b>NPCGuides</b></rainbow> <yellow>доступные команды:"
            val entry: String = "<dark_gray><b>*</b> <gold>/<label> <args> <gray>- <yellow><info><reset>"
        }

        @ConfigSerializable
        class Reset {
            val info: String = "<i>Удаляет данные игрока или всех игроков"
            val usage: String = "<red>Использование: /<label> reset [player|all]"
            val target: String = "<green>Ваши данные были удалены из базы данных, вы можете снова проходить гайды."
            val all: String = "<green>Данные всех игроков удалены из базы данных, они могут снова проходить гайды."
            val sender: String = "<green>Данные игрока <name> удалены, он может снова проходить гайды."
        }

        @ConfigSerializable
        class Reload {
            val info: String = "<i>Перезагружает конфигурацию плагина"
            val reloaded: String = "<green>Конфигурация плагина успешно перезагружена."
        }

        //@ConfigSerializable
        //class Info {
        //    val info: String = "<i>Получает информацию о гайде"
        //    val usage: String = "<red>Использование: /<label> info [player]"
        //}
    }
}
