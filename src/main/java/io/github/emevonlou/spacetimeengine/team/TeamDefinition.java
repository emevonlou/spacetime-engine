package io.github.emevonlou.spacetimeengine.team;

import io.github.emevonlou.spacetimeengine.map.MapPoint;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class TeamDefinition {

    private final String id;
    private final String displayName;
    private final MapPoint baseAnchor;
    private final MapPoint spawn;

    public TeamDefinition(
            String id,
            String displayName,
            MapPoint baseAnchor
    ) {
        this(
                id,
                displayName,
                baseAnchor,
                null
        );
    }

    public TeamDefinition(
            String id,
            String displayName,
            MapPoint baseAnchor,
            MapPoint spawn
    ) {
        this.id = normalizeId(id);

        this.displayName = requireText(
                displayName,
                "Team display name"
        );

        this.baseAnchor = Objects.requireNonNull(
                baseAnchor,
                "Team base anchor cannot be null."
        );

        this.spawn = spawn;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MapPoint getBaseAnchor() {
        return baseAnchor;
    }

    public Optional<MapPoint> getSpawn() {
        return Optional.ofNullable(spawn);
    }

    public static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Team id cannot be blank."
            );
        }

        String normalized =
                id.trim().toLowerCase(Locale.ROOT);

        if (!normalized.matches("[a-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Team id may only contain lowercase letters, "
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
