package io.github.emevonlou.spacetimeengine.map;

import io.github.emevonlou.spacetimeengine.team.TeamDefinition;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MapStorage {

    private static final String TROPICAL_PROBLEM_PARADOX =
            "maps/tropicalproblem_paradox.yml";

    private final JavaPlugin plugin;
    private final File mapsDirectory;

    public MapStorage(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "Plugin cannot be null."
        );

        this.mapsDirectory =
                new File(
                        plugin.getDataFolder(),
                        "maps"
                );
    }

    public void installBundledMaps() {
        if (
                !mapsDirectory.exists()
                        && !mapsDirectory.mkdirs()
        ) {
            throw new IllegalStateException(
                    "Could not create maps directory."
            );
        }

        File tropicalProblemParadox =
                new File(
                        mapsDirectory,
                        "tropicalproblem_paradox.yml"
                );

        if (!tropicalProblemParadox.exists()) {
            plugin.saveResource(
                    TROPICAL_PROBLEM_PARADOX,
                    false
            );
        }
    }

    public List<GameMapDefinition> loadMaps() {
        File[] files = mapsDirectory.listFiles(
                (directory, name) ->
                        name.toLowerCase()
                                .endsWith(".yml")
        );

        if (files == null) {
            return List.of();
        }

        List<File> sortedFiles =
                List.of(files)
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        File::getName
                                )
                        )
                        .toList();

        List<GameMapDefinition> maps =
                new ArrayList<>();

        for (File file : sortedFiles) {
            try {
                maps.add(
                        loadMap(file)
                );
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "Mapa ignorado em "
                                + file.getName()
                                + ": "
                                + exception.getMessage()
                );
            }
        }

        return List.copyOf(maps);
    }

    private GameMapDefinition loadMap(
            File file
    ) {
        YamlConfiguration configuration =
                YamlConfiguration.loadConfiguration(file);

        String id = requireString(
                configuration,
                "id"
        );

        String displayName = requireString(
                configuration,
                "display-name"
        );

        String worldName = requireString(
                configuration,
                "world"
        );

        String mode = requireString(
                configuration,
                "mode"
        );

        MapPoint center = readPoint(
                configuration,
                "center"
        );

        MapPoint spectatorSpawn = readPoint(
                configuration,
                "spectator-spawn"
        );

        ConfigurationSection teamsSection =
                requireSection(
                        configuration,
                        "teams"
                );

        Map<String, TeamDefinition> teams =
                new LinkedHashMap<>();

        for (
                String teamId
                : teamsSection.getKeys(false)
        ) {
            String normalizedTeamId =
                    TeamDefinition.normalizeId(teamId);

            String teamDisplayName =
                    teamsSection.getString(
                            teamId + ".display-name",
                            formatDisplayName(normalizedTeamId)
                    );

            TeamDefinition team =
                    new TeamDefinition(
                            normalizedTeamId,
                            teamDisplayName,
                            readPoint(
                                    teamsSection,
                                    teamId + ".base-anchor"
                            ),
                            readOptionalPoint(
                                    teamsSection,
                                    teamId + ".spawn"
                            )
                    );

            teams.put(
                    team.getId(),
                    team
            );
        }

        Map<String, MapPoint> diamondGenerators =
                readNamedPoints(
                        configuration,
                        "generators.diamond"
                );

        Map<String, MapPoint> emeraldGenerators =
                readNamedPoints(
                        configuration,
                        "generators.emerald"
                );

        return new GameMapDefinition(
                id,
                displayName,
                worldName,
                mode,
                center,
                spectatorSpawn,
                teams,
                diamondGenerators,
                emeraldGenerators
        );
    }

    private String formatDisplayName(
            String teamId
    ) {
        if (teamId.isEmpty()) {
            return teamId;
        }

        return Character.toUpperCase(
                teamId.charAt(0)
        ) + teamId.substring(1);
    }

    private Map<String, MapPoint> readNamedPoints(
            ConfigurationSection configuration,
            String path
    ) {
        ConfigurationSection section =
                requireSection(
                        configuration,
                        path
                );

        Map<String, MapPoint> points =
                new LinkedHashMap<>();

        for (String pointId : section.getKeys(false)) {
            points.put(
                    pointId.toLowerCase(),
                    readPoint(
                            section,
                            pointId
                    )
            );
        }

        return points;
    }

    private MapPoint readOptionalPoint(
            ConfigurationSection configuration,
            String path
    ) {
        if (
                configuration
                        .getConfigurationSection(path)
                        == null
        ) {
            return null;
        }

        return readPoint(
                configuration,
                path
        );
    }

    private MapPoint readPoint(
            ConfigurationSection configuration,
            String path
    ) {
        ConfigurationSection section =
                requireSection(
                        configuration,
                        path
                );

        if (
                !section.isSet("x")
                        || !section.isSet("y")
                        || !section.isSet("z")
        ) {
            throw new IllegalArgumentException(
                    "Point "
                            + path
                            + " must define x, y and z."
            );
        }

        return new MapPoint(
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble(
                        "yaw",
                        0.0
                ),
                (float) section.getDouble(
                        "pitch",
                        0.0
                )
        );
    }

    private String requireString(
            ConfigurationSection configuration,
            String path
    ) {
        String value =
                configuration.getString(path);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required field: " + path
            );
        }

        return value.trim();
    }

    private ConfigurationSection requireSection(
            ConfigurationSection configuration,
            String path
    ) {
        ConfigurationSection section =
                configuration.getConfigurationSection(
                        path
                );

        if (section == null) {
            throw new IllegalArgumentException(
                    "Missing required section: "
                            + path
            );
        }

        return section;
    }
}
