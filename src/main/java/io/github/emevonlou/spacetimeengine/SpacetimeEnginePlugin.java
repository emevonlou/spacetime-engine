package io.github.emevonlou.spacetimeengine;

import io.github.emevonlou.spacetimeengine.arena.ArenaManager;
import io.github.emevonlou.spacetimeengine.command.SpacetimeCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpacetimeEnginePlugin extends JavaPlugin {

    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        initializeArenaManager();
        registerCommands();

        getLogger().info("Spacetime Engine foi iniciado.");
        getLogger().info(
                "Em reverência a spacetime1000, um jogador extraordinário de PvP."
        );
    }

    @Override
    public void onDisable() {
        getLogger().info("Spacetime Engine foi encerrado.");
    }

    public ArenaManager getArenaManager() {
        return Objects.requireNonNull(
                arenaManager,
                "ArenaManager has not been initialized."
        );
    }

    private void initializeArenaManager() {
        arenaManager = new ArenaManager();
        arenaManager.createArena("development");

        getLogger().info(
                "Arena registrada: development"
        );
    }

    private void registerCommands() {
        PluginCommand spacetimeCommand = Objects.requireNonNull(
                getCommand("spacetime"),
                "O comando spacetime não foi encontrado no plugin.yml."
        );

        SpacetimeCommand executor = new SpacetimeCommand(this);

        spacetimeCommand.setExecutor(executor);
        spacetimeCommand.setTabCompleter(executor);
    }
}
