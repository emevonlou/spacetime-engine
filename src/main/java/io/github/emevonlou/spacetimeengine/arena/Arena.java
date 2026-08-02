package io.github.emevonlou.spacetimeengine.arena;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Arena {

    public static final int DEFAULT_MIN_PLAYERS = 2;
    public static final int DEFAULT_MAX_PLAYERS = 8;

    private final String id;
    private final Set<UUID> playerIds = new LinkedHashSet<>();

    private ArenaState state;
    private int minPlayers;
    private int maxPlayers;

    public Arena(String id) {
        this(
                id,
                DEFAULT_MIN_PLAYERS,
                DEFAULT_MAX_PLAYERS
        );
    }

    public Arena(
            String id,
            int minPlayers,
            int maxPlayers
    ) {
        this.id = normalizeId(id);

        validatePlayerLimits(
                minPlayers,
                maxPlayers
        );

        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.state = ArenaState.WAITING;
    }

    public String getId() {
        return id;
    }

    public ArenaState getState() {
        return state;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getPlayerCount() {
        return playerIds.size();
    }

    public Set<UUID> getPlayerIds() {
        return Set.copyOf(playerIds);
    }

    public boolean isFull() {
        return getPlayerCount() >= maxPlayers;
    }

    public boolean isAcceptingPlayers() {
        return state == ArenaState.WAITING
                || state == ArenaState.STARTING;
    }

    boolean addPlayer(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        return playerIds.add(playerId);
    }

    boolean removePlayer(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        return playerIds.remove(playerId);
    }

    public void updatePlayerLimits(
            int minPlayers,
            int maxPlayers
    ) {
        if (
                state != ArenaState.WAITING
                        && state != ArenaState.DISABLED
        ) {
            throw new IllegalStateException(
                    "Player limits can only be changed "
                            + "while the arena is waiting or disabled."
            );
        }

        validatePlayerLimits(
                minPlayers,
                maxPlayers
        );

        if (maxPlayers < getPlayerCount()) {
            throw new IllegalArgumentException(
                    "Maximum players cannot be lower "
                            + "than the current player count."
            );
        }

        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void transitionTo(ArenaState nextState) {
        Objects.requireNonNull(
                nextState,
                "Next arena state cannot be null."
        );

        if (!state.canTransitionTo(nextState)) {
            throw new IllegalStateException(
                    "Invalid arena state transition: "
                            + state
                            + " -> "
                            + nextState
            );
        }

        state = nextState;
    }

    public static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Arena id cannot be blank."
            );
        }

        String normalizedId = id
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!normalizedId.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Arena id may only contain lowercase letters, "
                            + "numbers, hyphens and underscores."
            );
        }

        return normalizedId;
    }

    public static void validatePlayerLimits(
            int minPlayers,
            int maxPlayers
    ) {
        if (minPlayers < 1) {
            throw new IllegalArgumentException(
                    "Minimum players must be at least 1."
            );
        }

        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException(
                    "Maximum players cannot be lower "
                            + "than minimum players."
            );
        }
    }
}
