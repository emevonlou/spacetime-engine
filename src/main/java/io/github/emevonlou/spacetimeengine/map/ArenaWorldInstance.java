package io.github.emevonlou.spacetimeengine.map;

import io.github.emevonlou.spacetimeengine.arena.Arena;

import java.util.Objects;

public final class ArenaWorldInstance {

    private static final String RUNTIME_WORLD_PREFIX = "ste_";

    private final String arenaId;
    private final String mapId;
    private final String sourceWorldName;
    private final String runtimeWorldName;

    private ArenaWorldInstance(
            String arenaId,
            String mapId,
            String sourceWorldName,
            String runtimeWorldName
    ) {
        this.arenaId = Objects.requireNonNull(
                arenaId,
                "Arena id cannot be null."
        );

        this.mapId = Objects.requireNonNull(
                mapId,
                "Map id cannot be null."
        );

        this.sourceWorldName = Objects.requireNonNull(
                sourceWorldName,
                "Source world name cannot be null."
        );

        this.runtimeWorldName = Objects.requireNonNull(
                runtimeWorldName,
                "Runtime world name cannot be null."
        );
    }

    public static ArenaWorldInstance from(
            Arena arena,
            GameMapDefinition map
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        Objects.requireNonNull(
                map,
                "Game map cannot be null."
        );

        String arenaMapId =
                arena.getMapId()
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Arena has no map assigned: "
                                                + arena.getId()
                                )
                        );

        if (!arenaMapId.equals(map.getId())) {
            throw new IllegalArgumentException(
                    "Arena map does not match game map: "
                            + arenaMapId
                            + " != "
                            + map.getId()
            );
        }

        String runtimeWorldName =
                RUNTIME_WORLD_PREFIX
                        + map.getId()
                        + "__"
                        + arena.getId();

        return new ArenaWorldInstance(
                arena.getId(),
                map.getId(),
                map.getWorldName(),
                runtimeWorldName
        );
    }

    public String getArenaId() {
        return arenaId;
    }

    public String getMapId() {
        return mapId;
    }

    public String getSourceWorldName() {
        return sourceWorldName;
    }

    public String getRuntimeWorldName() {
        return runtimeWorldName;
    }
}
