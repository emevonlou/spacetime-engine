package io.github.emevonlou.spacetimeengine.arena;

import java.util.Locale;
import java.util.Objects;

public final class Arena {

    private final String id;
    private ArenaState state;

    public Arena(String id) {
        this.id = normalizeId(id);
        this.state = ArenaState.WAITING;
    }

    public String getId() {
        return id;
    }

    public ArenaState getState() {
        return state;
    }

    public void transitionTo(ArenaState nextState) {
        Objects.requireNonNull(
                nextState,
                "Next arena state cannot be null."
        );

        if (!state.canTransitionTo(nextState)) {
            throw new IllegalStateException(
                    "Invalid arena state transition: "
                            + state
                            + " -> "
                            + nextState
            );
        }

        state = nextState;
    }

    public static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Arena id cannot be blank."
            );
        }

        return id.trim().toLowerCase(Locale.ROOT);
    }
}
