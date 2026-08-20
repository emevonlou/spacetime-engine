package io.github.emevonlou.spacetimeengine.team;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ArenaTeam {

    private final String arenaId;
    private final TeamDefinition definition;

    private final Set<UUID> playerIds =
            new LinkedHashSet<>();

    public ArenaTeam(
            String arenaId,
            TeamDefinition definition
    ) {
        if (arenaId == null || arenaId.isBlank()) {
            throw new IllegalArgumentException(
                    "Arena id cannot be blank."
            );
        }

        this.arenaId = arenaId;
        this.definition = Objects.requireNonNull(
                definition,
                "Team definition cannot be null."
        );
    }

    public String getArenaId() {
        return arenaId;
    }

    public String getId() {
        return definition.getId();
    }

    public TeamDefinition getDefinition() {
        return definition;
    }

    public int getPlayerCount() {
        return playerIds.size();
    }

    public Set<UUID> getPlayerIds() {
        return Set.copyOf(playerIds);
    }

    public boolean containsPlayer(UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        return playerIds.contains(playerId);
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

    void clearPlayers() {
        playerIds.clear();
    }
}
