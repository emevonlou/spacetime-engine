package io.github.emevonlou.spacetimeengine.map;

import java.util.Objects;

public final class ArenaWorldPreparationResult {

    private final ArenaWorldPreparationStatus status;
    private final ArenaWorldInstance instance;

    private ArenaWorldPreparationResult(
            ArenaWorldPreparationStatus status,
            ArenaWorldInstance instance
    ) {
        this.status = Objects.requireNonNull(
                status,
                "Arena world preparation status cannot be null."
        );

        this.instance = Objects.requireNonNull(
                instance,
                "Arena world instance cannot be null."
        );
    }

    public static ArenaWorldPreparationResult prepared(
            ArenaWorldInstance instance
    ) {
        return new ArenaWorldPreparationResult(
                ArenaWorldPreparationStatus.PREPARED,
                instance
        );
    }

    public static ArenaWorldPreparationResult runtimeAlreadyExists(
            ArenaWorldInstance instance
    ) {
        return new ArenaWorldPreparationResult(
                ArenaWorldPreparationStatus.RUNTIME_ALREADY_EXISTS,
                instance
        );
    }

    public static ArenaWorldPreparationResult sourceNotFound(
            ArenaWorldInstance instance
    ) {
        return new ArenaWorldPreparationResult(
                ArenaWorldPreparationStatus.SOURCE_NOT_FOUND,
                instance
        );
    }

    public static ArenaWorldPreparationResult preparationFailed(
            ArenaWorldInstance instance
    ) {
        return new ArenaWorldPreparationResult(
                ArenaWorldPreparationStatus.PREPARATION_FAILED,
                instance
        );
    }

    public ArenaWorldPreparationStatus getStatus() {
        return status;
    }

    public ArenaWorldInstance getInstance() {
        return instance;
    }

    public boolean isPrepared() {
        return status == ArenaWorldPreparationStatus.PREPARED;
    }
}
