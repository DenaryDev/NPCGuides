import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    kotlin("jvm") version "1.8.21"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0-SNAPSHOT"
}

group = "me.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://the-planet.fun/repo/snapshots/")
    maven("https://maven.citizensnpcs.co/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizensapi:2.0.31-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")

    library(kotlin("stdlib"))

    library("org.spongepowered:configurate-extra-kotlin:4.1.2")

    val crystalVersion = "2.0.0-SNAPSHOT"
    library("me.denarydev.crystal.shared:config:$crystalVersion")
    library("me.denarydev.crystal.shared:database:$crystalVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    jvmToolchain(17)
}

paper {
    main = "me.denarydev.npcguides.NPCGuidesPlugin"
    loader = "me.denarydev.npcguides.internal.PluginLibrariesLoader"
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
        register("npcguides.admin") {
            description = "Admin permission"
            default = BukkitPluginDescription.Permission.Default.FALSE
            children = listOf("npcguides.reload", "npcguides.reset", /*"npcguides.info"*/)
        }
        register("npcguides.reload") {
            description = "Allows to reload plugin"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("npcguides.reset") {
            description = "Allows to use /guides reset"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        //register("npcguides.info") {
        //    description = "Allows to use /guides info"
        //    default = BukkitPluginDescription.Permission.Default.OP
        //}
    }
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.19.4")
        runDirectory.set(project.projectDir.resolve("run/"))
    }
}
