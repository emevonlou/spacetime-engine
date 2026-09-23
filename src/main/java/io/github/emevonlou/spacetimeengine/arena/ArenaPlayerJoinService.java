package io.github.emevonlou.spacetimeengine.arena;

import io.github.emevonlou.spacetimeengine.map.ArenaWorldInstance;
import io.github.emevonlou.spacetimeengine.map.ArenaWorldPreparationResult;
import io.github.emevonlou.spacetimeengine.map.ArenaWorldPreparer;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.GameMapManager;
import io.github.emevonlou.spacetimeengine.map.MapWorldLoadResult;
import io.github.emevonlou.spacetimeengine.map.MapWorldManager;
import io.github.emevonlou.spacetimeengine.team.ArenaTeam;
import io.github.emevonlou.spacetimeengine.team.ArenaTeamManager;
import io.github.emevonlou.spacetimeengine.team.TeamSpawnResolution;
import io.github.emevonlou.spacetimeengine.team.TeamSpawnResolver;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class ArenaPlayerJoinService {

    private final ArenaPlayerManager playerManager;
    private final ArenaCountdownManager countdownManager;
    private final ArenaTeamManager teamManager;
    private final GameMapManager gameMapManager;
    private final ArenaWorldPreparer worldPreparer;
    private final MapWorldManager mapWorldManager;
    private final TeamSpawnResolver spawnResolver;

    public ArenaPlayerJoinService(
            ArenaPlayerManager playerManager,
            ArenaCountdownManager countdownManager,
            ArenaTeamManager teamManager,
            GameMapManager gameMapManager,
            ArenaWorldPreparer worldPreparer,
            MapWorldManager mapWorldManager,
            TeamSpawnResolver spawnResolver
    ) {
        this.playerManager = Objects.requireNonNull(
                playerManager,
                "ArenaPlayerManager cannot be null."
        );

        this.countdownManager = Objects.requireNonNull(
                countdownManager,
                "ArenaCountdownManager cannot be null."
        );

        this.teamManager = Objects.requireNonNull(
                teamManager,
                "ArenaTeamManager cannot be null."
        );

        this.gameMapManager = Objects.requireNonNull(
                gameMapManager,
                "GameMapManager cannot be null."
        );

        this.worldPreparer = Objects.requireNonNull(
                worldPreparer,
                "ArenaWorldPreparer cannot be null."
        );

        this.mapWorldManager = Objects.requireNonNull(
                mapWorldManager,
                "MapWorldManager cannot be null."
        );

        this.spawnResolver = Objects.requireNonNull(
                spawnResolver,
                "TeamSpawnResolver cannot be null."
        );
    }

    public ArenaPlayerJoinOutcome joinArena(
            Player player,
            Arena arena
    ) {
        Objects.requireNonNull(
                player,
                "Player cannot be null."
        );

        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        UUID playerId = player.getUniqueId();

        ArenaJoinResult joinResult =
                playerManager.joinArena(
                        playerId,
                        arena
                );

        ArenaPlayerJoinOutcome admissionOutcome =
                mapJoinResult(joinResult);

        if (
                admissionOutcome
                        != ArenaPlayerJoinOutcome.SUCCESS
        ) {
            return admissionOutcome;
        }

        /*
         * Preserve the existing behavior of development
         * arenas that do not have a map assigned.
         */
        if (arena.getMapId().isEmpty()) {
            return completeJoin(arena);
        }

        String mapId =
                arena.getMapId().orElseThrow();

        GameMapDefinition map =
                gameMapManager.findMap(mapId)
                        .orElse(null);

        if (map == null) {
            return rollback(
                    playerId,
                    ArenaPlayerJoinOutcome.MAP_UNAVAILABLE
            );
        }

        ArenaWorldInstance instance =
                ArenaWorldInstance.from(
                        arena,
                        map
                );

        ArenaWorldPreparationResult preparationResult =
                worldPreparer.prepare(instance);

        switch (preparationResult.getStatus()) {
            case SOURCE_NOT_FOUND -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome.WORLD_NOT_FOUND
                );
            }

            case PREPARATION_FAILED -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome.WORLD_LOAD_FAILED
                );
            }

            case PREPARED, RUNTIME_ALREADY_EXISTS -> {
                // Continue below.
            }
        }

        MapWorldLoadResult worldLoadResult =
                mapWorldManager.ensureLoaded(instance);

        switch (worldLoadResult.getStatus()) {
            case WORLD_NOT_FOUND -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome.WORLD_NOT_FOUND
                );
            }

            case LOAD_FAILED -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome.WORLD_LOAD_FAILED
                );
            }

            case ALREADY_LOADED, LOADED -> {
                // Continue below.
            }
        }

        ArenaTeam team =
                teamManager.findTeamByPlayer(playerId)
                        .orElse(null);

        if (team == null) {
            return rollback(
                    playerId,
                    ArenaPlayerJoinOutcome
                            .TEAM_ASSIGNMENT_UNAVAILABLE
            );
        }

        TeamSpawnResolution spawnResolution =
                spawnResolver.resolve(
                        instance,
                        team
                );

        switch (spawnResolution.getStatus()) {
            case SPAWN_NOT_CONFIGURED -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome
                                .SPAWN_NOT_CONFIGURED
                );
            }

            case WORLD_NOT_LOADED -> {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome
                                .WORLD_NOT_LOADED
                );
            }

            case SUCCESS -> {
                // Continue below.
            }
        }

        Location spawn =
                spawnResolution.getLocation()
                        .orElseThrow();

        try {
            if (!player.teleport(spawn)) {
                return rollback(
                        playerId,
                        ArenaPlayerJoinOutcome
                                .TELEPORT_FAILED
                );
            }
        } catch (RuntimeException exception) {
            rollbackPlayer(playerId);
            throw exception;
        }

        return completeJoin(arena);
    }

    private ArenaPlayerJoinOutcome completeJoin(
            Arena arena
    ) {
        countdownManager.evaluate(arena);

        return ArenaPlayerJoinOutcome.SUCCESS;
    }

    private ArenaPlayerJoinOutcome mapJoinResult(
            ArenaJoinResult result
    ) {
        return switch (result) {
            case SUCCESS ->
                    ArenaPlayerJoinOutcome.SUCCESS;

            case ALREADY_IN_ARENA ->
                    ArenaPlayerJoinOutcome.ALREADY_IN_ARENA;

            case ARENA_FULL ->
                    ArenaPlayerJoinOutcome.ARENA_FULL;

            case ARENA_NOT_ACCEPTING_PLAYERS ->
                    ArenaPlayerJoinOutcome
                            .ARENA_NOT_ACCEPTING_PLAYERS;

            case TEAM_ASSIGNMENT_UNAVAILABLE ->
                    ArenaPlayerJoinOutcome
                            .TEAM_ASSIGNMENT_UNAVAILABLE;
        };
    }

    private ArenaPlayerJoinOutcome rollback(
            UUID playerId,
            ArenaPlayerJoinOutcome outcome
    ) {
        rollbackPlayer(playerId);
        return outcome;
    }

    private void rollbackPlayer(UUID playerId) {
        if (playerManager.leaveArena(playerId).isEmpty()) {
            throw new IllegalStateException(
                    "Joined player could not be rolled back."
            );
        }
    }
}
