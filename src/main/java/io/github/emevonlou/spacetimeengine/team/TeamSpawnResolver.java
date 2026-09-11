package io.github.emevonlou.spacetimeengine.team;

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

        Objects.requireNonNull(
                team,
                "Arena team cannot be null."
        );

        Optional<MapPoint> spawn =
                team.getDefinition()
                        .getSpawn();

        if (spawn.isEmpty()) {
            return TeamSpawnResolution
                    .spawnNotConfigured();
        }

        Optional<Location> location =
                mapLocationResolver.resolve(
                        map,
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
