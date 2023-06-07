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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

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
    main = "me.example.plugin.ExamplePlugin"
    loader = "me.example.plugin.PluginLibrariesLoader"
    generateLibrariesJson = true
    author = "Me"
    apiVersion = "1.19"
    //defaultPermission = Default.OP

    //depends {
    //    register("FirstPlugin") {
    //        required = true
    //        bootstrap = false
    //    }
    //    register("SecondPlugin")
    //}

    //loadBefore {
    //    register("FirstPlugin")
    //}

    //loadAfter {
    //    register("SecondPlugin")
    //}

    //permissions {
    //    register("exampleplugin.admin") {
    //        description = "Admin permission"
    //        default = Default.FALSE
    //        children = listOf("exampleplugin.reload", "exampleplugin.use")
    //    }
    //    register("exampleplugin.reload") {
    //        description = "Allows to reload plugin"
    //    }
    //    register("exampleplugin.use") {
    //        description = "Allows to use plugin features"
    //    }
    //}
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
