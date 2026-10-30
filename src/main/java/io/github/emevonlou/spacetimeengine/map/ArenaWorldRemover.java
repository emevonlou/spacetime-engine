package io.github.emevonlou.spacetimeengine.map;

import org.bukkit.Server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class ArenaWorldRemover {

    private static final String RUNTIME_PREFIX = "ste_";

    private final Server server;
    private final Path worldContainer;

    public ArenaWorldRemover(Server server) {
        this.server = Objects.requireNonNull(
                server,
                "Server cannot be null."
        );

        this.worldContainer =
                server.getWorldContainer()
                        .toPath()
                        .toAbsolutePath()
                        .normalize();
    }

    public ArenaWorldRemovalStatus remove(
            ArenaWorldInstance instance
    ) {
        Objects.requireNonNull(
                instance,
                "Arena world instance cannot be null."
        );

        String runtimeWorldName =
                instance.getRuntimeWorldName();

        if (!runtimeWorldName.startsWith(RUNTIME_PREFIX)) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        if (
                runtimeWorldName.equals(
                        instance.getSourceWorldName()
                )
        ) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        Path relativeRuntime;

        try {
            relativeRuntime = Path.of(runtimeWorldName);
        } catch (InvalidPathException exception) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        if (
                relativeRuntime.isAbsolute()
                        || relativeRuntime.getNameCount() != 1
        ) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        Path runtimeDirectory =
                worldContainer
                        .resolve(relativeRuntime)
                        .normalize();

        Path sourceDirectory =
                worldContainer
                        .resolve(instance.getSourceWorldName())
                        .normalize();

        if (
                !runtimeDirectory.startsWith(worldContainer)
                        || runtimeDirectory.equals(worldContainer)
                        || runtimeDirectory.equals(sourceDirectory)
        ) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        if (server.getWorld(runtimeWorldName) != null) {
            return ArenaWorldRemovalStatus.WORLD_LOADED;
        }

        if (
                !Files.exists(
                        runtimeDirectory,
                        LinkOption.NOFOLLOW_LINKS
                )
        ) {
            return ArenaWorldRemovalStatus.NOT_FOUND;
        }

        if (
                Files.isSymbolicLink(runtimeDirectory)
                        || !Files.isDirectory(
                        runtimeDirectory,
                        LinkOption.NOFOLLOW_LINKS
                )
        ) {
            return ArenaWorldRemovalStatus.UNSAFE_RUNTIME;
        }

        try {
            deleteDirectory(runtimeDirectory);

            if (
                    Files.exists(
                            runtimeDirectory,
                            LinkOption.NOFOLLOW_LINKS
                    )
            ) {
                return ArenaWorldRemovalStatus.DELETE_FAILED;
            }

            return ArenaWorldRemovalStatus.DELETED;
        } catch (IOException | RuntimeException exception) {
            return ArenaWorldRemovalStatus.DELETE_FAILED;
        }
    }

    private void deleteDirectory(
            Path runtimeDirectory
    ) throws IOException {
        Files.walkFileTree(
                runtimeDirectory,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attributes
                    ) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path directory,
                            IOException exception
                    ) throws IOException {
                        if (exception != null) {
                            throw exception;
                        }

                        Files.deleteIfExists(directory);
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }
}
