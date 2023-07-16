import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.0"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

group = "me.rafaelka"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://the-planet.fun/repo/snapshots/")
    maven("https://maven.citizensnpcs.co/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizensapi:2.0.32-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")

    library("org.spongepowered:configurate-extra-kotlin:4.1.2")

    val crystalVersion = "2.0.0-SNAPSHOT"
    library("me.denarydev.crystal.shared:config:$crystalVersion")
    library("me.denarydev.crystal.shared:database:$crystalVersion")
}

kotlin {
    jvmToolchain(17)
}

paper {
    author = "RafaelkaUwU"

    main = "me.rafaelka.npcguides.NPCGuidesPlugin"
    loader = "me.rafaelka.npcguides.loader.PluginLibrariesLoader"

    generateLibrariesJson = true

    apiVersion = "1.20"

    serverDependencies {
        register("Citizens") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
        }
    }

    permissions {
        register("npcguides.admin") {
            description = "Admin permission"
            default = BukkitPluginDescription.Permission.Default.FALSE
            children = listOf("npcguides.reload", "npcguides.reset" /*"npcguides.info"*/)
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
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.20.1")
        val file = projectDir.resolve("run/server.jar")
        if (file.exists()) serverJar(file)
    }
}
