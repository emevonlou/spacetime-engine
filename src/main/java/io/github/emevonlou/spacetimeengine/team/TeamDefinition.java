package io.github.emevonlou.spacetimeengine.team;

import io.github.emevonlou.spacetimeengine.map.MapPoint;

import java.util.Locale;
import java.util.Objects;

public final class TeamDefinition {

    private final String id;
    private final String displayName;
    private final MapPoint baseAnchor;

    public TeamDefinition(
            String id,
            String displayName,
            MapPoint baseAnchor
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
