package io.github.emevonlou.spacetimeengine.map;

import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;

public final class MapWorldLoadResult {

    private final MapWorldLoadStatus status;
    private final World world;

    private MapWorldLoadResult(
            MapWorldLoadStatus status,
            World world
    ) {
        this.status = Objects.requireNonNull(
                status,
                "Map world load status cannot be null."
        );

        if (
                (
                        status == MapWorldLoadStatus.ALREADY_LOADED
                                || status == MapWorldLoadStatus.LOADED
                )
                        && world == null
        ) {
            throw new IllegalArgumentException(
                    "Successful world load result "
                            + "requires a world."
            );
        }

        if (
                (
                        status == MapWorldLoadStatus.WORLD_NOT_FOUND
                                || status == MapWorldLoadStatus.LOAD_FAILED
                )
                        && world != null
        ) {
            throw new IllegalArgumentException(
                    "Failed world load result "
                            + "cannot contain a world."
            );
        }

        this.world = world;
    }

    public static MapWorldLoadResult alreadyLoaded(
            World world
    ) {
        return new MapWorldLoadResult(
                MapWorldLoadStatus.ALREADY_LOADED,
                Objects.requireNonNull(
                        world,
                        "World cannot be null."
                )
        );
    }

    public static MapWorldLoadResult loaded(
            World world
    ) {
        return new MapWorldLoadResult(
                MapWorldLoadStatus.LOADED,
                Objects.requireNonNull(
                        world,
                        "World cannot be null."
                )
        );
    }

    public static MapWorldLoadResult worldNotFound() {
        return new MapWorldLoadResult(
                MapWorldLoadStatus.WORLD_NOT_FOUND,
                null
        );
    }

    public static MapWorldLoadResult loadFailed() {
        return new MapWorldLoadResult(
                MapWorldLoadStatus.LOAD_FAILED,
                null
        );
    }

    public MapWorldLoadStatus getStatus() {
        return status;
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    public boolean isLoaded() {
        return status == MapWorldLoadStatus.ALREADY_LOADED
                || status == MapWorldLoadStatus.LOADED;
    }
}
