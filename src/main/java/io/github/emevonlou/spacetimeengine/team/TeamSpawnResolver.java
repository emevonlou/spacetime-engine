package io.github.emevonlou.spacetimeengine.team;

import io.github.emevonlou.spacetimeengine.map.ArenaWorldInstance;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.MapLocationResolver;
import io.github.emevonlou.spacetimeengine.map.MapPoint;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

public final class TeamSpawnResolver {

    private final MapLocationResolver mapLocationResolver;

    public TeamSpawnResolver(
            MapLocationResolver mapLocationResolver
    ) {
        this.mapLocationResolver =
                Objects.requireNonNull(
                        mapLocationResolver,
                        "MapLocationResolver cannot be null."
                );
    }

    public TeamSpawnResolution resolve(
            GameMapDefinition map,
            ArenaTeam team
    ) {
        Objects.requireNonNull(
                map,
                "Game map cannot be null."
        );

        return resolve(
                team,
                point ->
                        mapLocationResolver.resolve(
                                map,
                                point
                        )
        );
    }

    public TeamSpawnResolution resolve(
            ArenaWorldInstance instance,
            ArenaTeam team
    ) {
        Objects.requireNonNull(
                instance,
                "Arena world instance cannot be null."
        );

        return resolve(
                team,
                point ->
                        mapLocationResolver.resolve(
                                instance,
                                point
                        )
        );
    }

    private TeamSpawnResolution resolve(
            ArenaTeam team,
            java.util.function.Function<
                    MapPoint,
                    Optional<Location>
                    > locationResolver
    ) {
        Objects.requireNonNull(
                team,
                "Arena team cannot be null."
        );

        Objects.requireNonNull(
                locationResolver,
                "Location resolver cannot be null."
        );

        Optional<MapPoint> spawn =
                team.getDefinition()
                        .getSpawn();

        if (spawn.isEmpty()) {
            return TeamSpawnResolution
                    .spawnNotConfigured();
        }

        Optional<Location> location =
                locationResolver.apply(
                        spawn.get()
                );

        if (location.isEmpty()) {
            return TeamSpawnResolution
                    .worldNotLoaded();
        }

        return TeamSpawnResolution.success(
                location.get()
        );
    }
}
