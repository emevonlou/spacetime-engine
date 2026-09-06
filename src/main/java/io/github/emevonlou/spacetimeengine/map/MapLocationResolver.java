package io.github.emevonlou.spacetimeengine.map;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;

public final class MapLocationResolver {

    private final Server server;

    public MapLocationResolver(Server server) {
        this.server = Objects.requireNonNull(
                server,
                "Server cannot be null."
        );
    }

    public Optional<Location> resolve(
            GameMapDefinition map,
            MapPoint point
    ) {
        Objects.requireNonNull(
                map,
                "Game map cannot be null."
        );

        Objects.requireNonNull(
                point,
                "Map point cannot be null."
        );

        World world =
                server.getWorld(
                        map.getWorldName()
                );

        if (world == null) {
            return Optional.empty();
        }

        return Optional.of(
                new Location(
                        world,
                        point.x(),
                        point.y(),
                        point.z(),
                        point.yaw(),
                        point.pitch()
                )
        );
    }
}
