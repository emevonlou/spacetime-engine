package io.github.emevonlou.spacetimeengine.team;

import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

public final class TeamSpawnResolution {

    private final TeamSpawnResolutionStatus status;
    private final Location location;

    private TeamSpawnResolution(
            TeamSpawnResolutionStatus status,
            Location location
    ) {
        this.status = Objects.requireNonNull(
                status,
                "Spawn resolution status cannot be null."
        );

        if (
                status == TeamSpawnResolutionStatus.SUCCESS
                        && location == null
        ) {
            throw new IllegalArgumentException(
                    "Successful spawn resolution "
                            + "requires a location."
            );
        }

        if (
                status != TeamSpawnResolutionStatus.SUCCESS
                        && location != null
        ) {
            throw new IllegalArgumentException(
                    "Failed spawn resolution "
                            + "cannot contain a location."
            );
        }

        this.location = location;
    }

    public static TeamSpawnResolution success(
            Location location
    ) {
        return new TeamSpawnResolution(
                TeamSpawnResolutionStatus.SUCCESS,
                Objects.requireNonNull(
                        location,
                        "Location cannot be null."
                )
        );
    }

    public static TeamSpawnResolution spawnNotConfigured() {
        return new TeamSpawnResolution(
                TeamSpawnResolutionStatus.SPAWN_NOT_CONFIGURED,
                null
        );
    }

    public static TeamSpawnResolution worldNotLoaded() {
        return new TeamSpawnResolution(
                TeamSpawnResolutionStatus.WORLD_NOT_LOADED,
                null
        );
    }

    public TeamSpawnResolutionStatus getStatus() {
        return status;
    }

    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    public boolean isSuccess() {
        return status == TeamSpawnResolutionStatus.SUCCESS;
    }
}
