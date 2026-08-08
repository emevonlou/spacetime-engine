package io.github.emevonlou.spacetimeengine.arena;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ArenaLifecycleManager {

    private static final long ENDING_DELAY_TICKS = 40L;
    private static final long RESETTING_DELAY_TICKS = 40L;

    private final JavaPlugin plugin;

    private final Map<String, BukkitTask> lifecycleTasks =
            new HashMap<>();

    public ArenaLifecycleManager(JavaPlugin plugin) {
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
                arena.getState() == ArenaState.RUNNING
                        && arena.getPlayerCount() == 0
        ) {
            finishEmptyArena(arena);
        }
    }

    private void finishEmptyArena(Arena arena) {
        if (lifecycleTasks.containsKey(arena.getId())) {
            return;
        }

        arena.transitionTo(ArenaState.ENDING);

        plugin.getLogger().info(
                "Arena "
                        + arena.getId()
                        + " entrou em ENDING: "
                        + "nenhum jogador restante."
        );

        BukkitTask endingTask =
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (
                                arena.getState()
                                        != ArenaState.ENDING
                        ) {
                            lifecycleTasks.remove(
                                    arena.getId()
                            );

                            return;
                        }

                        arena.transitionTo(
                                ArenaState.RESETTING
                        );

                        plugin.getLogger().info(
                                "Arena "
                                        + arena.getId()
                                        + " entrou em RESETTING."
                        );

                        scheduleWaiting(arena);
                    }
                }.runTaskLater(
                        plugin,
                        ENDING_DELAY_TICKS
                );

        lifecycleTasks.put(
                arena.getId(),
                endingTask
        );
    }

    private void scheduleWaiting(Arena arena) {
        BukkitTask resettingTask =
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (
                                arena.getState()
                                        != ArenaState.RESETTING
                        ) {
                            lifecycleTasks.remove(
                                    arena.getId()
                            );

                            return;
                        }

                        arena.transitionTo(
                                ArenaState.WAITING
                        );

                        lifecycleTasks.remove(
                                arena.getId()
                        );

                        plugin.getLogger().info(
                                "Arena "
                                        + arena.getId()
                                        + " foi resetada para WAITING."
                        );
                    }
                }.runTaskLater(
                        plugin,
                        RESETTING_DELAY_TICKS
                );

        lifecycleTasks.put(
                arena.getId(),
                resettingTask
        );
    }

    public void cancelAll() {
        for (BukkitTask task : lifecycleTasks.values()) {
            task.cancel();
        }

        lifecycleTasks.clear();
    }
}
