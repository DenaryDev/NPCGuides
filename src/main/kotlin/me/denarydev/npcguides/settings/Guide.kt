package me.denarydev.npcguides.settings

import me.denarydev.npcguides.LOGGER
import org.bukkit.Particle
import org.bukkit.Sound
import org.jetbrains.annotations.ApiStatus.Internal
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
class Guide {

    @Transient
    var id: String = "madadm"
    fun id(id: String): Guide {
        this.id = id
        return this
    }

    @Setting("a-npc-id")
    @Comment("ID NPC, к которому привязан гайд")
    val npcId: Int = 0

    //val hologramHeight: Int = 1,
    @Comment("Действие по умолчанию, если ни одно действие из списка не подходит")
    val default: PermissionAction = PermissionAction(
        PermissionAction.ChatAction(listOf("<yellow>[NPC] <white>Я сейчас занят, загляни позднее!")),
        PermissionAction.ParticleAction(Particle.VILLAGER_ANGRY),
        PermissionAction.SoundAction(Sound.ENTITY_VILLAGER_NO),
        PermissionAction.ExecuteAction(false, listOf())
    )

    @Comment("Список действий")
    val interactions: List<Interaction> = listOf(Interaction())
}

@ConfigSerializable
class Interaction {
    @Comment(
        """Число или диапазон, при каком по счёту взаимодействии срабатывает это действие
Допустимые форматы:
* 1 - Действие будет выполняться только при первом взаимодействии
* 1-3 - Действие будет выполняться при первом, втором и третьем взаимодействиях
* 3+ - Действие будет выполняться при третьем и последующих взаимодействиях"""
    )
    val range: String = "1-3"

    @Comment(
        """Разрешение, необходимое для выполнения действия
Оставьте пустым, чтобы отключить"""
    )
    val permission: String = "guides.madadm.first"

    @Comment(
        """Действие, которое воспроизводится, если у игрока есть разрешение
Если разрешение не установлено, то выполняется это действие"""
    )
    val hasPermission: PermissionAction = PermissionAction(
        PermissionAction.ChatAction(
            messages = listOf(
                "<yellow>[NPC] <white>Привет путник!",
                "<yellow>[NPC] <white>Меня зовут Идель!",
                "<yellow>[NPC] Идель<white>: Я твой проводник в этот мир!"
            )
        )
    )

    @Comment("Действие, которое воспроизводится, если у игрока нет разрешения")
    val noPermission: PermissionAction = PermissionAction(
        PermissionAction.ChatAction(listOf("<yellow>[NPC] <white>Я сейчас занят, загляни попозже!")),
        PermissionAction.ParticleAction(Particle.VILLAGER_ANGRY),
        PermissionAction.SoundAction(Sound.ENTITY_VILLAGER_NO),
        PermissionAction.ExecuteAction(true, listOf("console;say Идель сейчас занят!"))
    )

    fun inRange(talks: Int): Boolean {
        return if (this.range.endsWith("+")) {
            val i = range.replace("+", "").toInt()
            talks >= i
        } else {
            if (this.range.contains("-")) {
                val split = range.split("-")
                if (split.size == 2) {
                    val first = split[0].toInt()
                    val second = split[1].toInt()
                    first..second
                } else {
                    LOGGER.error("Invalid range: $range")
                    return false
                }
            } else {
                val i = range.toInt()
                i..i
            }.contains(talks)
        }
    }
}

@ConfigSerializable
class PermissionAction() {
    constructor(chat: ChatAction) : this() {
        this.chat = chat
    }

    constructor(chat: ChatAction, particle: ParticleAction, sound: SoundAction, execute: ExecuteAction) : this() {
        this.chat = chat
        this.particle = particle
        this.sound = sound
        this.execute = execute
    }

    @Transient
    var permission: String = ""

    fun permission(permission: String): PermissionAction {
        this.permission = permission
        return this
    }

    @Comment("Настройки отправки сообщений в чат")
    var chat: ChatAction = ChatAction()

    @Comment("Настройки частиц над нпс")
    var particle: ParticleAction = ParticleAction()

    @Comment("Настройки звука, который воспроизводится при взаимодействии")
    var sound: SoundAction = SoundAction()

    @Comment("Настройки действий, выполняемых после отправки всех сообщений")
    var execute: ExecuteAction = ExecuteAction()

    @ConfigSerializable
    class ChatAction() {
        constructor(messages: List<String>) : this() {
            this.messages = messages
        }

        @Comment("Интервал между сообщениями в секундах")
        val interval: Long = 2

        @Comment("Воспроизводить-ли звук после каждого сообщения?")
        val sound: Boolean = true

        @Comment(
            """Список сообщений. Отправляются в том же порядке, что и в списке
По умолчанию доступен только один заполнитель: %player_name%,
но так же поддерживаются заполнители PlaceholderAPI, если он есть
Поддерживается форматирование через MiniMessage (https://docs.advntr.dev/minimessage/format.html)"""
        )
        var messages: List<String> = listOf()
    }

    @ConfigSerializable
    class ParticleAction() {
        constructor(type: Particle) : this() {
            this.type = type
        }

        @Comment("Тип частиц. Все доступные частицы: https://jd.papermc.io/paper/1.19/org/bukkit/Particle.html")
        var type: Particle = Particle.VILLAGER_HAPPY

        @Comment("Количество частиц")
        val count: Int = 5

        @Comment("Высота частиц над нпс")
        val height: Double = 0.8
    }

    @ConfigSerializable
    class SoundAction() {
        constructor(type: Sound) : this() {
            this.type = type
        }

        @Comment("Тип звука. Все доступные звуки: https://jd.papermc.io/paper/1.19/org/bukkit/Sound.html")
        var type: Sound = Sound.ENTITY_VILLAGER_YES

        @Comment("Громкость звука")
        val volume: Float = 1F

        @Comment("Тональность звука")
        val pitch: Float = 1F
    }

    @ConfigSerializable
    class ExecuteAction() {
        constructor(sound: Boolean, actions: List<String>) : this() {
            this.sound = sound
            this.actions = actions
        }

        @Comment("Воспроизводить-ли звук после выполнения всех действий?")
        var sound: Boolean = false

        @Comment(
            """Список действий. Выполняется в том же порядке, что и в списке.
По умолчанию доступен только один заполнитель: %player_name%,
но так же поддерживаются заполнители PlaceholderAPI, если он есть
Формат: <тип-действия>;<контекст>
* player - Выполняет команду от имени игрока
* console - Выполняет команду от имени консоли
* message - Отправляет сообщение игроку.
          Поддерживается форматирование через MiniMessage (https://docs.advntr.dev/minimessage/format.html)"""
        )
        var actions: List<String> = listOf(
            "console;say Выполнение команды от имени консоли",
            "player;say Выполнение команды от имени игрока",
            "message;<yellow>Действие отправки сообщения"
        )
    }
}

@Internal
enum class PermissionActionType {
    HAS, NO, DEFAULT
}
