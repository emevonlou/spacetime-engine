package io.github.emevonlou.spacetimeengine.map;

import org.bukkit.Server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Set;

public final class ArenaWorldPreparer {

    private static final Set<String> EXCLUDED_ROOT_FILES =
            Set.of(
                    "uid.dat",
                    "session.lock"
            );

    private final Path worldContainer;

    public ArenaWorldPreparer(Server server) {
        Objects.requireNonNull(
                server,
                "Server cannot be null."
        );

        this.worldContainer =
                server.getWorldContainer()
                        .toPath()
                        .toAbsolutePath()
                        .normalize();
    }

    public ArenaWorldPreparationResult prepare(
            ArenaWorldInstance instance
    ) {
        Objects.requireNonNull(
                instance,
                "Arena world instance cannot be null."
        );

        Path sourceDirectory =
                worldContainer
                        .resolve(instance.getSourceWorldName())
                        .normalize();

        Path runtimeDirectory =
                worldContainer
                        .resolve(instance.getRuntimeWorldName())
                        .normalize();

        if (
                !isSafeWorldDirectory(sourceDirectory)
                        || !isSafeWorldDirectory(runtimeDirectory)
                        || sourceDirectory.equals(runtimeDirectory)
        ) {
            return ArenaWorldPreparationResult
                    .preparationFailed(instance);
        }

        Path sourceLevelData =
                sourceDirectory.resolve("level.dat");

        if (
                !Files.isDirectory(sourceDirectory)
                        || !Files.isRegularFile(sourceLevelData)
        ) {
            return ArenaWorldPreparationResult
                    .sourceNotFound(instance);
        }

        if (
                Files.exists(
                        runtimeDirectory,
                        LinkOption.NOFOLLOW_LINKS
                )
        ) {
            return ArenaWorldPreparationResult
                    .runtimeAlreadyExists(instance);
        }

        if (Files.isSymbolicLink(sourceDirectory)) {
            return ArenaWorldPreparationResult
                    .preparationFailed(instance);
        }

        try {
            Path realWorldContainer =
                    worldContainer.toRealPath();

            Path realSourceDirectory =
                    sourceDirectory.toRealPath();

            if (
                    !realSourceDirectory.startsWith(
                            realWorldContainer
                    )
                            || realSourceDirectory.equals(
                            realWorldContainer
                    )
            ) {
                return ArenaWorldPreparationResult
                        .preparationFailed(instance);
            }

            copyWorldDirectory(
                    realSourceDirectory,
                    runtimeDirectory
            );

            return ArenaWorldPreparationResult
                    .prepared(instance);
        } catch (IOException | RuntimeException exception) {
            deletePartialRuntime(runtimeDirectory);

            return ArenaWorldPreparationResult
                    .preparationFailed(instance);
        }
    }

    private boolean isSafeWorldDirectory(Path path) {
        return path.startsWith(worldContainer)
                && !path.equals(worldContainer);
    }

    private void copyWorldDirectory(
            Path sourceDirectory,
            Path runtimeDirectory
    ) throws IOException {
        Files.walkFileTree(
                sourceDirectory,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                            Path directory,
                            BasicFileAttributes attributes
                    ) throws IOException {
                        Path relative =
                                sourceDirectory.relativize(
                                        directory
                                );

                        Path targetDirectory =
                                runtimeDirectory.resolve(
                                        relative.toString()
                                );

                        Files.createDirectories(
                                targetDirectory
                        );

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attributes
                    ) throws IOException {
                        if (Files.isSymbolicLink(file)) {
                            return FileVisitResult.CONTINUE;
                        }

                        Path relative =
                                sourceDirectory.relativize(file);

                        if (
                                relative.getNameCount() == 1
                                        && EXCLUDED_ROOT_FILES
                                        .contains(
                                                relative.toString()
                                        )
                        ) {
                            return FileVisitResult.CONTINUE;
                        }

                        Path targetFile =
                                runtimeDirectory.resolve(
                                        relative.toString()
                                );

                        Files.copy(
                                file,
                                targetFile,
                                StandardCopyOption.COPY_ATTRIBUTES
                        );

                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    private void deletePartialRuntime(
            Path runtimeDirectory
    ) {
        if (!isSafeWorldDirectory(runtimeDirectory)) {
            return;
        }

        if (
                !Files.exists(
                        runtimeDirectory,
                        LinkOption.NOFOLLOW_LINKS
                )
        ) {
            return;
        }

        try {
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
        } catch (IOException ignored) {
            /*
             * Preparation already failed.
             * A leftover directory will be detected as
             * RUNTIME_ALREADY_EXISTS on the next attempt.
             */
        }
    }
}
