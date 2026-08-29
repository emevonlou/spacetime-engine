package io.github.emevonlou.spacetimeengine.team;

import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.GameMapManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ArenaTeamManager {

    private final GameMapManager gameMapManager;

    private final Map<String, Map<String, ArenaTeam>>
            teamsByArena = new LinkedHashMap<>();

    private final Map<UUID, ArenaTeam> playerTeams =
            new HashMap<>();

    public ArenaTeamManager(
            GameMapManager gameMapManager
    ) {
        this.gameMapManager = Objects.requireNonNull(
                gameMapManager,
                "GameMapManager cannot be null."
        );
    }

    public List<ArenaTeam> getTeams(
            Arena arena
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        return List.copyOf(
                ensureArenaTeams(arena).values()
        );
    }

    public Optional<ArenaTeam> findTeam(
            Arena arena,
            String teamId
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        if (teamId == null || teamId.isBlank()) {
            return Optional.empty();
        }

        String normalizedTeamId =
                TeamDefinition.normalizeId(teamId);

        return Optional.ofNullable(
                ensureArenaTeams(arena)
                        .get(normalizedTeamId)
        );
    }

    public Optional<ArenaTeam> findTeamByPlayer(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        return Optional.ofNullable(
                playerTeams.get(playerId)
        );
    }

    public Optional<ArenaTeam> assignPlayer(
            Arena arena,
            UUID playerId
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        if (!arena.getPlayerIds().contains(playerId)) {
            throw new IllegalStateException(
                    "Player must belong to the arena "
                            + "before team assignment."
            );
        }

        ArenaTeam existing =
                playerTeams.get(playerId);

        if (existing != null) {
            if (
                    existing.getArenaId()
                            .equals(arena.getId())
            ) {
                return Optional.of(existing);
            }

            throw new IllegalStateException(
                    "Player is already assigned "
                            + "to another arena team."
            );
        }

        List<ArenaTeam> teams =
                getTeams(arena);

        if (teams.isEmpty()) {
            return Optional.empty();
        }

        ArenaTeam selected =
                selectLeastPopulatedTeam(teams);

        if (!selected.addPlayer(playerId)) {
            throw new IllegalStateException(
                    "Player could not be added "
                            + "to team "
                            + selected.getId()
                            + "."
            );
        }

        playerTeams.put(
                playerId,
                selected
        );

        return Optional.of(selected);
    }

    public Optional<ArenaTeam> removePlayer(
            Arena arena,
            UUID playerId
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        Objects.requireNonNull(
                playerId,
                "Player UUID cannot be null."
        );

        ArenaTeam team =
                playerTeams.get(playerId);

        if (team == null) {
            return Optional.empty();
        }

        if (
                !team.getArenaId()
                        .equals(arena.getId())
        ) {
            throw new IllegalStateException(
                    "Player team does not belong "
                            + "to arena "
                            + arena.getId()
                            + "."
            );
        }

        playerTeams.remove(playerId);
        team.removePlayer(playerId);

        return Optional.of(team);
    }

    public int getTeamCount(
            Arena arena
    ) {
        return getTeams(arena).size();
    }

    public void clearArena(
            Arena arena
    ) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        Map<String, ArenaTeam> teams =
                teamsByArena.remove(
                        arena.getId()
                );

        if (teams == null) {
            return;
        }

        for (ArenaTeam team : teams.values()) {
            for (UUID playerId : team.getPlayerIds()) {
                playerTeams.remove(playerId);
            }

            team.clearPlayers();
        }
    }

    public void clear() {
        for (
                Map<String, ArenaTeam> teams
                : teamsByArena.values()
        ) {
            for (ArenaTeam team : teams.values()) {
                team.clearPlayers();
            }
        }

        playerTeams.clear();
        teamsByArena.clear();
    }

    private ArenaTeam selectLeastPopulatedTeam(
            List<ArenaTeam> teams
    ) {
        ArenaTeam selected = null;

        for (ArenaTeam team : teams) {
            if (
                    selected == null
                            || team.getPlayerCount()
                            < selected.getPlayerCount()
            ) {
                selected = team;
            }
        }

        if (selected == null) {
            throw new IllegalStateException(
                    "No arena team is available."
            );
        }

        return selected;
    }

    private Map<String, ArenaTeam> ensureArenaTeams(
            Arena arena
    ) {
        Map<String, ArenaTeam> existing =
                teamsByArena.get(
                        arena.getId()
                );

        if (existing != null) {
            return existing;
        }

        Map<String, ArenaTeam> created =
                createArenaTeams(arena);

        teamsByArena.put(
                arena.getId(),
                created
        );

        return created;
    }

    private Map<String, ArenaTeam> createArenaTeams(
            Arena arena
    ) {
        if (arena.getMapId().isEmpty()) {
            return Map.of();
        }

        String mapId =
                arena.getMapId().orElseThrow();

        GameMapDefinition map =
                gameMapManager.findMap(mapId)
                        .orElse(null);

        if (map == null) {
            return Map.of();
        }

        Map<String, ArenaTeam> teams =
                new LinkedHashMap<>();

        for (
                TeamDefinition definition
                : map.getTeams().values()
        ) {
            ArenaTeam arenaTeam =
                    new ArenaTeam(
                            arena.getId(),
                            definition
                    );

            teams.put(
                    arenaTeam.getId(),
                    arenaTeam
            );
        }

        return teams;
    }
}
