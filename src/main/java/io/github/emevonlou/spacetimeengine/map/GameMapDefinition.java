package io.github.emevonlou.spacetimeengine.map;

import io.github.emevonlou.spacetimeengine.team.TeamDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Optional;

public final class GameMapDefinition {

    private final String id;
    private final String displayName;
    private final String worldName;
    private final String mode;

    private final MapPoint center;
    private final MapPoint spectatorSpawn;

    private final Map<String, TeamDefinition> teams;
    private final Map<String, MapPoint> diamondGenerators;
    private final Map<String, MapPoint> emeraldGenerators;

    public GameMapDefinition(
            String id,
            String displayName,
            String worldName,
            String mode,
            MapPoint center,
            MapPoint spectatorSpawn,
            Map<String, TeamDefinition> teams,
            Map<String, MapPoint> diamondGenerators,
            Map<String, MapPoint> emeraldGenerators
    ) {
        this.id = normalizeId(id);

        this.displayName = requireText(
                displayName,
                "Map display name"
        );

        this.worldName = requireText(
                worldName,
                "World name"
        );

        this.mode = requireText(
                mode,
                "Map mode"
        );

        this.center = Objects.requireNonNull(
                center,
                "Map center cannot be null."
        );

        this.spectatorSpawn = Objects.requireNonNull(
                spectatorSpawn,
                "Spectator spawn cannot be null."
        );

        this.teams = Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Objects.requireNonNull(
                                teams,
                                "Teams cannot be null."
                        )
                )
        );

        this.diamondGenerators = Map.copyOf(
                Objects.requireNonNull(
                        diamondGenerators,
                        "Diamond generators cannot be null."
                )
        );

        this.emeraldGenerators = Map.copyOf(
                Objects.requireNonNull(
                        emeraldGenerators,
                        "Emerald generators cannot be null."
                )
        );
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getMode() {
        return mode;
    }

    public MapPoint getCenter() {
        return center;
    }

    public MapPoint getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public Map<String, TeamDefinition> getTeams() {
        return teams;
    }

    public Map<String, MapPoint> getDiamondGenerators() {
        return diamondGenerators;
    }

    public Map<String, MapPoint> getEmeraldGenerators() {
        return emeraldGenerators;
    }

    public Optional<TeamDefinition> findTeam(
            String id
    ) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String normalizedId =
                TeamDefinition.normalizeId(id);

        return Optional.ofNullable(
                teams.get(normalizedId)
        );
    }

    public List<String> getTeamIds() {
        return List.copyOf(
                teams.keySet()
        );
    }

    public int getTeamCount() {
        return teams.size();
    }

    public static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Map id cannot be blank."
            );
        }

        String normalized =
                id.trim().toLowerCase(Locale.ROOT);

        if (!normalized.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Map id may only contain lowercase letters, "
                            + "numbers, hyphens and underscores."
            );
        }

        return normalized;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank."
            );
        }

        return value.trim();
    }
}
