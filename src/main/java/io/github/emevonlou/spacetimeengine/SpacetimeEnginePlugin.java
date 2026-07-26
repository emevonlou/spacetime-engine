package io.github.emevonlou.spacetimeengine;

import org.bukkit.plugin.java.JavaPlugin;

public final class SpacetimeEnginePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Spacetime Engine foi iniciado.");
        getLogger().info("Em reverência a spacetime1000, um jogador extraordinário de PvP.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Spacetime Engine foi encerrado.");
    }
}
