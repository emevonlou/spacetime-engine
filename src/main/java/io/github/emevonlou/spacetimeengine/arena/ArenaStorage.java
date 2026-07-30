package io.github.emevonlou.spacetimeengine.arena;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class ArenaStorage {

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

    public List<String> loadArenaIds() {
        if (!file.exists()) {
            return List.of();
        }

        YamlConfiguration configuration =
                YamlConfiguration.loadConfiguration(file);

        return List.copyOf(
                configuration.getStringList("arenas")
        );
    }

    public boolean saveArenas(Collection<Arena> arenas) {
        Objects.requireNonNull(
                arenas,
                "Arena collection cannot be null."
        );

        if (
                !plugin.getDataFolder().exists()
                        && !plugin.getDataFolder().mkdirs()
        ) {
            plugin.getLogger().severe(
                    "Não foi possível criar a pasta de dados do plugin."
            );

            return false;
        }

        YamlConfiguration configuration =
                new YamlConfiguration();

        configuration.set("schema-version", 1);

        configuration.set(
                "arenas",
                arenas.stream()
                        .map(Arena::getId)
                        .sorted()
                        .toList()
        );

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
