package io.github.emevonlou.spacetimeengine.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ArenaPlayerManager {

    private final ArenaManager arenaManager;
    private final ArenaCountdownManager countdownManager;

    private final Map<UUID, String> playerArenas =
            new HashMap<>();

    public ArenaPlayerManager(
            ArenaManager arenaManager,
            ArenaCountdownManager countdownManager
    ) {
        this.arenaManager = Objects.requireNonNull(
                arenaManager,
                "ArenaManager cannot be null."
        );

        this.countdownManager = Objects.requireNonNull(
                countdownManager,
                "ArenaCountdownManager cannot be null."
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

        countdownManager.evaluate(arena);

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

        arena.ifPresent(value -> {
            value.removePlayer(playerId);
            countdownManager.evaluate(value);
        });

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
                : Set.copyOf(playerArenas.keySet())
        ) {
            leaveArena(playerId);
        }
    }
}
