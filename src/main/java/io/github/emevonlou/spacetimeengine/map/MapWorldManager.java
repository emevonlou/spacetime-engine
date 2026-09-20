package io.github.emevonlou.spacetimeengine.map;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class MapWorldManager {

    private final Server server;
    private final Path worldContainer;

    public MapWorldManager(Server server) {
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

    public MapWorldLoadResult ensureLoaded(
            GameMapDefinition map
    ) {
        Objects.requireNonNull(
                map,
                "Game map cannot be null."
        );

        return ensureLoadedWorld(
                map.getWorldName()
        );
    }

    public MapWorldLoadResult ensureLoaded(
            ArenaWorldInstance instance
    ) {
        Objects.requireNonNull(
                instance,
                "Arena world instance cannot be null."
        );

        return ensureLoadedWorld(
                instance.getRuntimeWorldName()
        );
    }

    private MapWorldLoadResult ensureLoadedWorld(
            String worldName
    ) {
        World loadedWorld =
                server.getWorld(worldName);

        if (loadedWorld != null) {
            return MapWorldLoadResult
                    .alreadyLoaded(loadedWorld);
        }

        Path worldDirectory =
                worldContainer
                        .resolve(worldName)
                        .normalize();

        if (!worldDirectory.startsWith(worldContainer)) {
            return MapWorldLoadResult.loadFailed();
        }

        Path levelData =
                worldDirectory.resolve("level.dat");

        if (
                !Files.isDirectory(worldDirectory)
                        || !Files.isRegularFile(levelData)
        ) {
            return MapWorldLoadResult.worldNotFound();
        }

        try {
            World world =
                    server.createWorld(
                            new WorldCreator(worldName)
                    );

            if (world == null) {
                return MapWorldLoadResult.loadFailed();
            }

            return MapWorldLoadResult.loaded(world);
        } catch (RuntimeException exception) {
            return MapWorldLoadResult.loadFailed();
        }
    }
}
