package me.example.plugin;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.LibraryLoadingException;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class PluginLibrariesLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder builder) {
        final var source = builder.getContext().getPluginSource();
        final var libraries = load(source);
        if (libraries != null) {
            final var repositories = libraries.asRepositories();
            if (repositories == null)
                throw new LibraryLoadingException("Failed to load repositories from 'paper-libraries.json' in %s".formatted(source));
            final var dependencies = libraries.asDependencies();
            if (dependencies == null)
                throw new LibraryLoadingException("Failed to load dependencies from 'paper-libraries.json' in %s".formatted(source));
            final var resolver = new MavenLibraryResolver();
            repositories.forEach(resolver::addRepository);
            dependencies.forEach(resolver::addDependency);
            builder.addLibrary(resolver);
        }
    }

    private PluginLibraries load(Path source) {
        try (final var in = getClass().getResourceAsStream("/paper-libraries.json")) {
            if (in == null)
                throw new LibraryLoadingException("File 'paper-libraries.json' not found in %s".formatted(source));
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record PluginLibraries(Map<String, String> repositories, List<String> dependencies) {
        public Stream<RemoteRepository> asRepositories() {
            return repositories.entrySet().stream()
                    .map(e -> new RemoteRepository.Builder(e.getKey(), "default", e.getValue()).build());
        }

        public Stream<Dependency> asDependencies() {
            return dependencies.stream()
                    .map(d -> new Dependency(new DefaultArtifact(d), null));
        }
    }
}