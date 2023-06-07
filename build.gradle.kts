import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0-SNAPSHOT"
}

group = "me.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://the-planet.fun/repo/snapshots/")
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.31-SNAPSHOT")

    val crystalVersion = "2.0.0-SNAPSHOT"
    library("me.denarydev.crystal.paper:utils:$crystalVersion")
    library("me.denarydev.crystal.shared:config:$crystalVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

paper {
    main = "me.denarydev.npcguldes.NPCGuidesPlugin"
    loader = "me.denarydev.npcguldes.PluginLibrariesLoader"
    generateLibrariesJson = true
    author = "Me"
    apiVersion = "1.19"
    defaultPermission = BukkitPluginDescription.Permission.Default.OP

    depends {
        register("Citizens")
    }

    loadAfter {
        register("Citizens")
    }

    permissions {
        register("npcguldes.admin") {
            description = "Admin permission"
            default = BukkitPluginDescription.Permission.Default.FALSE
            children = listOf("npcguldes.reload", "npcguldes.use")
        }
        register("npcguldes.reload") {
            description = "Allows to reload plugin"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("npcguldes.use") {
            description = "Allows to use plugin features"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(
            listOf(
                "-parameters",
                "-nowarn",
                "-Xlint:-unchecked",
                "-Xlint:-deprecation",
                "-Xlint:-processing"
            )
        )
        options.isFork = true
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.19.4")
        runDirectory.set(project.projectDir.resolve("run/"))
    }
}
