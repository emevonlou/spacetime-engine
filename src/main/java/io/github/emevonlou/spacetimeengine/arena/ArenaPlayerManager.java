package io.github.emevonlou.spacetimeengine.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ArenaPlayerManager {

    private final ArenaManager arenaManager;

    private final Map<UUID, String> playerArenas =
            new HashMap<>();

    public ArenaPlayerManager(
            ArenaManager arenaManager
    ) {
        this.arenaManager = Objects.requireNonNull(
                arenaManager,
                "ArenaManager cannot be null."
        );
    }

    public ArenaJoinResult joinArena(
            UUID playerId,
            Arena arena
    ) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        if (playerArenas.containsKey(playerId)) {
            return ArenaJoinResult.ALREADY_IN_ARENA;
        }

        if (!arena.isAcceptingPlayers()) {
            return ArenaJoinResult.ARENA_NOT_ACCEPTING_PLAYERS;
        }

        if (arena.isFull()) {
            return ArenaJoinResult.ARENA_FULL;
        }

        if (!arena.addPlayer(playerId)) {
            return ArenaJoinResult.ALREADY_IN_ARENA;
        }

        playerArenas.put(
                playerId,
                arena.getId()
        );

        return ArenaJoinResult.SUCCESS;
    }

    public Optional<Arena> leaveArena(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        String arenaId =
                playerArenas.remove(playerId);

        if (arenaId == null) {
            return Optional.empty();
        }

        Optional<Arena> arena =
                arenaManager.findArena(arenaId);

        arena.ifPresent(value ->
                value.removePlayer(playerId)
        );

        return arena;
    }

    public Optional<Arena> findArenaByPlayer(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        String arenaId =
                playerArenas.get(playerId);

        if (arenaId == null) {
            return Optional.empty();
        }

        return arenaManager.findArena(arenaId);
    }

    public void clear() {
        for (
                UUID playerId
                : SetCopy.of(playerArenas)
        ) {
            leaveArena(playerId);
        }
    }

    private static final class SetCopy {

        private SetCopy() {
        }

        static Iterable<UUID> of(
                Map<UUID, String> map
        ) {
            return java.util.Set.copyOf(
                    map.keySet()
            );
        }
    }
}
