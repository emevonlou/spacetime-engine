package io.github.emevonlou.spacetimeengine.arena;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class ArenaStorage {

    private static final int SCHEMA_VERSION = 2;

    private final JavaPlugin plugin;
    private final File file;

    public ArenaStorage(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "Plugin cannot be null."
        );

        this.file = new File(
                plugin.getDataFolder(),
                "arenas.yml"
        );
    }

    public List<Arena> loadArenas() {
        if (!file.exists()) {
            return List.of();
        }

        YamlConfiguration configuration =
                YamlConfiguration.loadConfiguration(file);

        if (configuration.isList("arenas")) {
            return loadLegacyArenas(configuration);
        }

        ConfigurationSection arenasSection =
                configuration.getConfigurationSection(
                        "arenas"
                );

        if (arenasSection == null) {
            return List.of();
        }

        List<Arena> arenas = new ArrayList<>();

        for (String arenaId : arenasSection.getKeys(false)) {
            int minPlayers = arenasSection.getInt(
                    arenaId + ".min-players",
                    Arena.DEFAULT_MIN_PLAYERS
            );

            int maxPlayers = arenasSection.getInt(
                    arenaId + ".max-players",
                    Arena.DEFAULT_MAX_PLAYERS
            );

            try {
                arenas.add(
                        new Arena(
                                arenaId,
                                minPlayers,
                                maxPlayers
                        )
                );
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "Arena ignorada em arenas.yml: "
                                + exception.getMessage()
                );
            }
        }

        return List.copyOf(arenas);
    }

    private List<Arena> loadLegacyArenas(
            YamlConfiguration configuration
    ) {
        List<Arena> arenas = new ArrayList<>();

        for (
                String arenaId
                : configuration.getStringList("arenas")
        ) {
            try {
                arenas.add(
                        new Arena(arenaId)
                );
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "Arena antiga ignorada: "
                                + exception.getMessage()
                );
            }
        }

        plugin.getLogger().info(
                "Formato antigo de arenas.yml detectado. "
                        + "O arquivo será migrado."
        );

        return List.copyOf(arenas);
    }

    public boolean saveArenas(
            Collection<Arena> arenas
    ) {
        Objects.requireNonNull(
                arenas,
                "Arena collection cannot be null."
        );

        if (
                !plugin.getDataFolder().exists()
                        && !plugin.getDataFolder().mkdirs()
        ) {
            plugin.getLogger().severe(
                    "Não foi possível criar a pasta "
                            + "de dados do plugin."
            );

            return false;
        }

        YamlConfiguration configuration =
                new YamlConfiguration();

        configuration.set(
                "schema-version",
                SCHEMA_VERSION
        );

        arenas.stream()
                .sorted(
                        Comparator.comparing(
                                Arena::getId
                        )
                )
                .forEach(arena -> {
                    String path =
                            "arenas." + arena.getId();

                    configuration.set(
                            path + ".min-players",
                            arena.getMinPlayers()
                    );

                    configuration.set(
                            path + ".max-players",
                            arena.getMaxPlayers()
                    );
                });

        try {
            configuration.save(file);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Não foi possível salvar arenas.yml.",
                    exception
            );

            return false;
        }
    }

    public File getFile() {
        return file;
    }
}
