package io.github.emevonlou.spacetimeengine.arena;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ArenaManager {

    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public Arena createArena(String id) {
        return registerArena(new Arena(id));
    }

    public Arena registerArena(Arena arena) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        Arena existingArena = arenas.putIfAbsent(
                arena.getId(),
                arena
        );

        if (existingArena != null) {
            throw new IllegalArgumentException(
                    "Arena already registered: " + arena.getId()
            );
        }

        return arena;
    }

    public Optional<Arena> findArena(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String normalizedId = Arena.normalizeId(id);

        return Optional.ofNullable(
                arenas.get(normalizedId)
        );
    }

    public List<Arena> getArenas() {
        return List.copyOf(arenas.values());
    }

    public List<String> getArenaIds() {
        return List.copyOf(arenas.keySet());
    }

    public int size() {
        return arenas.size();
    }
}
