package io.github.emevonlou.spacetimeengine;

import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.arena.ArenaManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaStorage;
import io.github.emevonlou.spacetimeengine.command.SpacetimeCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpacetimeEnginePlugin
        extends JavaPlugin {

    private ArenaManager arenaManager;
    private ArenaStorage arenaStorage;

    @Override
    public void onEnable() {
        initializeArenaSystem();
        registerCommands();

        getLogger().info(
                "Spacetime Engine foi iniciado."
        );

        getLogger().info(
                "Em reverência a spacetime1000, "
                        + "um jogador extraordinário de PvP."
        );
    }

    @Override
    public void onDisable() {
        if (
                arenaManager != null
                        && arenaStorage != null
        ) {
            saveArenas();
        }

        getLogger().info(
                "Spacetime Engine foi encerrado."
        );
    }

    public ArenaManager getArenaManager() {
        return Objects.requireNonNull(
                arenaManager,
                "ArenaManager has not been initialized."
        );
    }

    public boolean saveArenas() {
        return Objects.requireNonNull(
                arenaStorage,
                "ArenaStorage has not been initialized."
        ).saveArenas(
                getArenaManager().getArenas()
        );
    }

    private void initializeArenaSystem() {
        arenaManager = new ArenaManager();
        arenaStorage = new ArenaStorage(this);

        int loadedArenas = 0;

        for (Arena arena : arenaStorage.loadArenas()) {
            try {
                arenaManager.registerArena(arena);
                loadedArenas++;
            } catch (IllegalArgumentException exception) {
                getLogger().warning(
                        "Arena ignorada: "
                                + exception.getMessage()
                );
            }
        }

        if (arenaManager.size() == 0) {
            arenaManager.createArena("development");

            getLogger().info(
                    "Arena padrão registrada: development"
            );
        } else {
            getLogger().info(
                    "Arenas carregadas: " + loadedArenas
            );
        }

        if (!saveArenas()) {
            getLogger().warning(
                    "As arenas não puderam ser salvas "
                            + "durante a inicialização."
            );
        }
    }

    private void registerCommands() {
        PluginCommand spacetimeCommand =
                Objects.requireNonNull(
                        getCommand("spacetime"),
                        "O comando spacetime não foi "
                                + "encontrado no plugin.yml."
                );

        SpacetimeCommand executor =
                new SpacetimeCommand(this);

        spacetimeCommand.setExecutor(executor);
        spacetimeCommand.setTabCompleter(executor);
    }
}
