package io.github.emevonlou.spacetimeengine.team;

import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.GameMapManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ArenaTeamManager {

    private final GameMapManager gameMapManager;

    private final Map<String, Map<String, ArenaTeam>>
            teamsByArena = new LinkedHashMap<>();

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

        teamsByArena.clear();
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
