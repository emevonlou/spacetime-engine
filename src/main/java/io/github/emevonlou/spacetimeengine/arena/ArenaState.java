package io.github.emevonlou.spacetimeengine.arena;

public enum ArenaState {

    WAITING,
    STARTING,
    RUNNING,
    ENDING,
    RESETTING,
    DISABLED;

    public boolean canTransitionTo(ArenaState nextState) {
        if (nextState == null || nextState == this) {
            return false;
        }

        return switch (this) {
            case WAITING ->
                    nextState == STARTING
                            || nextState == DISABLED;

            case STARTING ->
                    nextState == RUNNING
                            || nextState == WAITING
                            || nextState == DISABLED;

            case RUNNING ->
                    nextState == ENDING
                            || nextState == DISABLED;

            case ENDING ->
                    nextState == RESETTING
                            || nextState == DISABLED;

            case RESETTING ->
                    nextState == WAITING
                            || nextState == DISABLED;

            case DISABLED ->
                    nextState == WAITING;
        };
    }
}
