/*
 * Copyright (c) 2024 DenaryDev
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package me.denarydev.npcguides.settings

import me.denarydev.crystal.config.CrystalConfigs
import me.denarydev.crystal.db.DatabaseType
import me.denarydev.npcguides.logger
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
    guides.clear()
    if (!Files.exists(path)) Files.createDirectories(path)
    val files = Files.list(path).use { it.toList() }
    if (files != null && files.isNotEmpty()) {
        files.forEach {
            if (it != null && it.name.endsWith(".conf")) {
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
    logger.info("Loaded ${guides.size} guides")
}

@ConfigSerializable
class MainConfig {
    @Comment("Режим отладки")
    val debug = true

    @Comment("Интервал появления частиц над нпс (в тиках)")
    val particlesInterval = 5L

    @Comment("Настройки базы данных")
    val database = Database()

    @ConfigSerializable
    class Database {
        @Comment("Тип базы данных. Доступно: SQLITE, MYSQL")
        val type = DatabaseType.SQLITE

        @Comment("Настройки удалённого подключения (для MySQL)")
        val remote = Remote()

        @ConfigSerializable
        class Remote {
            @Comment("Настройки адреса бд")
            val connection = Connection()

            @Comment("Пользователь, используемый для подключения к бд")
            val credentials = Credentials()

            @Comment("Основные параметры подключения")
            val settings = Settings()

            @Comment(
                """Дополнительные параметры, которые вы можете использовать.
Можно добавить следующие параметры:
  useSSL = false
  verifyServerCertificate = false"""
            )
            val properties = mutableMapOf(
                "useUnicode" to "true",
                "characterEncoding" to "utf8"
            )

            @ConfigSerializable
            class Connection {
                @Comment("Адрес удалённого сервера")
                val address = "localhost"

                @Comment("Порт удалённого сервера")
                val port = 3306

                @Comment("База данных, в которой будут храниться данные")
                val database = "minecraft"
            }

            @ConfigSerializable
            class Credentials {
                @Comment("Имя пользователя")
                val username = "root"

                @Comment("Пароль")
                val password = ""
            }

            @ConfigSerializable
            class Settings {
                @Comment(
                    """Максимальное количество одновременных подключений.
Должно быть так же, сколько у вас ядер.
Значение по умолчанию: 6"""
                )
                val maxPoolSize = 6

                @Comment(
                    """Количество соединений, которые всегда должны быть открыты.
Чтобы избежать проблем, установите то же значение, что и maxPoolSize.
Значение по умолчанию: 6"""
                )
                val minimumIdle = 6

                @Comment(
                    """Количество миллисекунд, в течение которых одно соединение должно оставаться открытым.
Значение по умолчанию: 1800000"""
                )
                val maxLifeTime = 1800000

                @Comment(
                    """Установка интервала, в течение которого нужно «пинговать» базу данных. Установите 0, чтобы отключить.
Значение по умолчанию: 0"""
                )
                val keepAliveTime = 0

                @Comment(
                    """Количество секунд, в течение которых мы ждем ответа от базы данных, прежде чем истечет время ожидания.
Значение по умолчанию: 500"""
                )
                val connectionTimeout = 5000
            }
        }
    }
}

@ConfigSerializable
class MessagesConfig {
    @Comment("Сообщения о различных ошибках")
    val errors = Errors()

    @Comment("Сообщения для команд")
    val commands = Commands()

    @ConfigSerializable
    class Errors {
        //val notFound = "<red>Игрок <arg> не найден."
        val guideNotFound = "<red>Гайд <arg> не найден."
        //val playersOnly = "<red>Эту команду могут использовать только игроки."
        //val noPerm = "<red>У вас недостаточно прав, чтобы использовать эту команду."
        val noData = "<red>Данные о игроке не найдены"
    }

    @ConfigSerializable
    class Commands {
        //val guides = Guides()
        //val help = Help()
        val reset = Reset()
        val reload = Reload()
        val about = About()
        //val info = Info()

        //@ConfigSerializable
        //class Guides {
        //    val usage = "<red>Используйте /<label> help для просмотра списка доступных команд"
        //}

        //@ConfigSerializable
        //class Help {
        //    val header = "<rainbow><b>NPCGuides</b></rainbow> <yellow>доступные команды:"
        //    val entry = "<dark_gray><b>*</b> <gold>/<label> <args> <gray>- <yellow><info><reset>"
        //}

        @ConfigSerializable
        class Reset {
            //val info = "<i>Удаляет данные игрока или всех игроков"
            //val usage = "<red>Использование: /<label> reset [player|all]"
            val target = "<yellow>[Система] <gray>Гайды: <white>Ваши данные были удалены из базы данных, вы можете снова проходить гайды."
            //val all = "<yellow>[Система] <gray>Гайды: <white>Данные всех игроков удалены из базы данных, они могут снова проходить гайды."
            val sender = "<yellow>[Система] <gray>Гайды: <white>Данные игрока <name> удалены, он может снова проходить гайды."
        }

        @ConfigSerializable
        class Reload {
            //val info = "<i>Перезагружает конфигурацию плагина"
            val reloaded = "<yellow>[Система] <gray>Гайды: <white>Конфигурация плагина успешно перезагружена."
        }

        @ConfigSerializable
        class About {
            val success = """
            <yellow>[Система] <gray>Гайды: <white>Информация о плагине:
            <white>Версия: <yellow><version>
            <white>Платформа: <yellow><platform>
            <white>Создатель: <yellow><author>
            <white>Дата и время сборки: <yellow><build_time>
            <white>Информация о сборке: <yellow>git-<commit> <white>в ветке <yellow><branch>
            """.trimIndent()
        }

        //@ConfigSerializable
        //class Info {
        //    val info = "<i>Получает информацию о гайде"
        //    val usage = "<red>Использование: /<label> info [player]"
        //}
    }
}