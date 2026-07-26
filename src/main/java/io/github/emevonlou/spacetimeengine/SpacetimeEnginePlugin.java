package io.github.emevonlou.spacetimeengine;

import io.github.emevonlou.spacetimeengine.command.SpacetimeCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpacetimeEnginePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
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

    private void registerCommands() {
        Objects.requireNonNull(
                getCommand("spacetime"),
                "O comando spacetime não foi encontrado no plugin.yml."
        ).setExecutor(new SpacetimeCommand(this));
    }
}
