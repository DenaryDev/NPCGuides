import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "1.9.23"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("io.papermc.paperweight.userdev") version "1.5.13"
    id("org.ajoberstar.grgit") version "5.2.2"
    id("net.kyori.blossom") version "2.1.0"
}

group = "me.denarydev"
version = "1.20.2-v1"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.activmine.ru/public/")
    maven("https://maven.citizensnpcs.co/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizensapi:2.0.33-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")

    library("org.spongepowered:configurate-extra-kotlin:4.1.2")
    library(kotlin("stdlib"))

    val crystalVersion = "2.1.1"
    library("me.denarydev.crystal.shared:config:$crystalVersion")
    library("me.denarydev.crystal.shared:database:$crystalVersion")
}

kotlin {
    jvmToolchain(17)
}

paper {
    author = "DenaryDev"

    main = "me.denarydev.npcguides.NPCGuidesPlugin"
    loader = "me.denarydev.npcguides.loader.PluginLibrariesLoader"

    generateLibrariesJson = true

    apiVersion = "1.20"

    serverDependencies {
        register("Citizens") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
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

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", rootProject.version.toString())
                property("build_time", System.currentTimeMillis().toString())
                property("git_branch", grgit.branch.current().name)
                property("git_commit", shortCommit())
            }
        }
    }
}

fun shortCommit(): String {
    val clean = grgit.status().isClean
    val commit = grgit.head().abbreviatedId
    return commit + (if (clean) "" else "-dirty")
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    build {
        dependsOn(reobfJar)
    }

    withType<RunServer>().configureEach {
        minecraftVersion("1.20.2")
        val file = projectDir.resolve("run/server.jar")
        if (file.exists()) serverJar(file)

        downloadPlugins {
            // Don't download these plugins on Folia because they don't support Folia.
            if (this@configureEach.name == "runServer") {
                url("https://download.luckperms.net/1521/bukkit/loader/LuckPerms-Bukkit-5.4.108.jar")
                url("https://repo.extendedclip.com/content/repositories/placeholderapi/me/clip/placeholderapi/2.11.5/placeholderapi-2.11.5.jar")
                url("https://ci.citizensnpcs.co/job/Citizens2/3281/artifact/dist/target/Citizens-2.0.33-b3281.jar")
            }
        }
    }
}