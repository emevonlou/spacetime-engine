package io.github.emevonlou.spacetimeengine.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ArenaCountdownManager {

    private static final int COUNTDOWN_SECONDS = 10;

    private final JavaPlugin plugin;

    private final Map<String, BukkitTask> countdownTasks =
            new HashMap<>();

    public ArenaCountdownManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "Plugin cannot be null."
        );
    }

    public void evaluate(Arena arena) {
        Objects.requireNonNull(
                arena,
                "Arena cannot be null."
        );

        if (
                arena.getState() == ArenaState.WAITING
                        && arena.getPlayerCount()
                        >= arena.getMinPlayers()
        ) {
            startCountdown(arena);
            return;
        }

        if (
                arena.getState() == ArenaState.STARTING
                        && arena.getPlayerCount()
                        < arena.getMinPlayers()
        ) {
            cancelCountdown(
                    arena,
                    "Countdown cancelled: not enough players."
            );
        }
    }

    private void startCountdown(Arena arena) {
        if (countdownTasks.containsKey(arena.getId())) {
            return;
        }

        arena.transitionTo(ArenaState.STARTING);

        broadcast(
                arena,
                Component.text(
                        "Enough players! Match countdown started.",
                        NamedTextColor.GREEN
                )
        );

        BukkitRunnable runnable = new BukkitRunnable() {

            private int remainingSeconds =
                    COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (arena.getState() != ArenaState.STARTING) {
                    countdownTasks.remove(arena.getId());
                    cancel();
                    return;
                }

                if (
                        arena.getPlayerCount()
                                < arena.getMinPlayers()
                ) {
                    cancelCountdown(
                            arena,
                            "Countdown cancelled: not enough players."
                    );

                    cancel();
                    return;
                }

                if (remainingSeconds <= 0) {
                    arena.transitionTo(ArenaState.RUNNING);

                    countdownTasks.remove(arena.getId());

                    broadcast(
                            arena,
                            Component.text(
                                    "Match started!",
                                    NamedTextColor.GREEN
                            )
                    );

                    plugin.getLogger().info(
                            "Arena "
                                    + arena.getId()
                                    + " iniciou a partida."
                    );

                    cancel();
                    return;
                }

                broadcast(
                        arena,
                        Component.text(
                                "Match starts in "
                                        + remainingSeconds
                                        + "...",
                                NamedTextColor.YELLOW
                        )
                );

                remainingSeconds--;
            }
        };

        BukkitTask task = runnable.runTaskTimer(
                plugin,
                0L,
                20L
        );

        countdownTasks.put(
                arena.getId(),
                task
        );
    }

    private void cancelCountdown(
            Arena arena,
            String message
    ) {
        BukkitTask task =
                countdownTasks.remove(arena.getId());

        if (task != null) {
            task.cancel();
        }

        if (arena.getState() == ArenaState.STARTING) {
            arena.transitionTo(ArenaState.WAITING);
        }

        broadcast(
                arena,
                Component.text(
                        message,
                        NamedTextColor.RED
                )
        );

        plugin.getLogger().info(
                "Contagem cancelada na arena "
                        + arena.getId()
                        + "."
        );
    }

    private void broadcast(
            Arena arena,
            Component message
    ) {
        for (UUID playerId : arena.getPlayerIds()) {
            Player player =
                    plugin.getServer().getPlayer(playerId);

            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }

    public void cancelAll() {
        for (BukkitTask task : countdownTasks.values()) {
            task.cancel();
        }

        countdownTasks.clear();
    }
}
