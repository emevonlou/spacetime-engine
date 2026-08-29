package io.github.emevonlou.spacetimeengine;

import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.arena.ArenaCountdownManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaLifecycleManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaPlayerManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaStorage;
import io.github.emevonlou.spacetimeengine.command.SpacetimeCommand;
import io.github.emevonlou.spacetimeengine.listener.PlayerConnectionListener;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.GameMapManager;
import io.github.emevonlou.spacetimeengine.map.MapStorage;
import io.github.emevonlou.spacetimeengine.team.ArenaTeamManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SpacetimeEnginePlugin
        extends JavaPlugin {

    private ArenaManager arenaManager;
    private ArenaCountdownManager arenaCountdownManager;
    private ArenaLifecycleManager arenaLifecycleManager;
    private ArenaPlayerManager arenaPlayerManager;
    private ArenaStorage arenaStorage;
    private GameMapManager gameMapManager;
    private MapStorage mapStorage;
    private ArenaTeamManager arenaTeamManager;

    @Override
    public void onEnable() {
        initializeArenaSystem();
        initializeMapSystem();
        initializeTeamSystem();
        initializePlayerSystem();

        registerCommands();
        registerListeners();

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
        if (arenaPlayerManager != null) {
            arenaPlayerManager.clear();
        }

        if (arenaCountdownManager != null) {
            arenaCountdownManager.cancelAll();
        }

        if (arenaLifecycleManager != null) {
            arenaLifecycleManager.cancelAll();
        }

        if (arenaTeamManager != null) {
            arenaTeamManager.clear();
        }

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

    public GameMapManager getGameMapManager() {
        return Objects.requireNonNull(
                gameMapManager,
                "GameMapManager has not been initialized."
        );
    }

    public ArenaTeamManager getArenaTeamManager() {
        return Objects.requireNonNull(
                arenaTeamManager,
                "ArenaTeamManager has not been initialized."
        );
    }

    public ArenaPlayerManager getArenaPlayerManager() {
        return Objects.requireNonNull(
                arenaPlayerManager,
                "ArenaPlayerManager has not been initialized."
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

    private void initializeMapSystem() {
        mapStorage = new MapStorage(this);
        mapStorage.installBundledMaps();

        gameMapManager = new GameMapManager();

        int loadedMaps = 0;

        for (
                GameMapDefinition map
                : mapStorage.loadMaps()
        ) {
            try {
                gameMapManager.registerMap(map);
                loadedMaps++;
            } catch (IllegalArgumentException exception) {
                getLogger().warning(
                        "Mapa ignorado: "
                                + exception.getMessage()
                );
            }
        }

        getLogger().info(
                "Mapas carregados: " + loadedMaps
        );
    }

    private void initializeTeamSystem() {
        arenaTeamManager =
                new ArenaTeamManager(
                        getGameMapManager()
                );
    }

    private void initializePlayerSystem() {
        arenaCountdownManager =
                new ArenaCountdownManager(this);

        arenaLifecycleManager =
                new ArenaLifecycleManager(this);

        arenaPlayerManager =
                new ArenaPlayerManager(
                        getArenaManager(),
                        arenaCountdownManager,
                        arenaLifecycleManager,
                        getArenaTeamManager()
                );
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

    private void registerListeners() {
        getServer()
                .getPluginManager()
                .registerEvents(
                        new PlayerConnectionListener(
                                getArenaPlayerManager()
                        ),
                        this
                );
    }
}
