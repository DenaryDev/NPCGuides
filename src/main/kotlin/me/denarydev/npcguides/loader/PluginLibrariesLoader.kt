@file:Suppress("UnstableApiUsage", "unused")

package me.denarydev.npcguides.loader

import com.google.gson.Gson
import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.LibraryLoadingException
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.stream.Stream

class PluginLibrariesLoader : PluginLoader {
    override fun classloader(builder: PluginClasspathBuilder) {
        val libraries = load(builder.context.pluginSource)
        val resolver = MavenLibraryResolver()
        libraries.asRepositories().forEach(resolver::addRepository)
        libraries.asDependencies().forEach(resolver::addDependency)
        builder.addLibrary(resolver)
    }

    private fun load(source: Path): PluginLibraries {
        javaClass.getResourceAsStream("/paper-libraries.json").use {
            if (it == null) throw LibraryLoadingException("File 'paper-libraries.json' not found in $source")
            return Gson().fromJson(InputStreamReader(it, StandardCharsets.UTF_8), PluginLibraries::class.java)
        }
    }
}

private data class PluginLibraries(val repositories: Map<String, String>, val dependencies: List<String>) {
    fun asRepositories(): Stream<RemoteRepository> {
        return repositories.entries.stream()
            .map {
                RemoteRepository.Builder(it.key, "default", it.value).build()
            }
    }

    fun asDependencies(): Stream<Dependency> {
        return dependencies.stream()
            .map {
                Dependency(DefaultArtifact(it), null)
            }
    }
}