package io.github.emevonlou.spacetimeengine.map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GameMapManager {

    private final Map<String, GameMapDefinition> maps =
            new LinkedHashMap<>();

    public GameMapDefinition registerMap(
            GameMapDefinition map
    ) {
        Objects.requireNonNull(
                map,
                "Game map cannot be null."
        );

        GameMapDefinition existing =
                maps.putIfAbsent(
                        map.getId(),
                        map
                );

        if (existing != null) {
            throw new IllegalArgumentException(
                    "Map already registered: "
                            + map.getId()
            );
        }

        return map;
    }

    public Optional<GameMapDefinition> findMap(
            String id
    ) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                maps.get(
                        GameMapDefinition.normalizeId(id)
                )
        );
    }

    public List<GameMapDefinition> getMaps() {
        return List.copyOf(
                maps.values()
        );
    }

    public List<String> getMapIds() {
        return List.copyOf(
                maps.keySet()
        );
    }

    public int size() {
        return maps.size();
    }
}
