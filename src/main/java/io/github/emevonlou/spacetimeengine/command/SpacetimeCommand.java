package io.github.emevonlou.spacetimeengine.command;

import io.github.emevonlou.spacetimeengine.SpacetimeEnginePlugin;
import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.arena.ArenaManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaJoinResult;
import io.github.emevonlou.spacetimeengine.arena.ArenaState;
import io.github.emevonlou.spacetimeengine.map.GameMapDefinition;
import io.github.emevonlou.spacetimeengine.map.MapPoint;
import io.github.emevonlou.spacetimeengine.team.ArenaTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class SpacetimeCommand
        implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS =
            List.of(
                    "arena",
                    "arenas",
                    "create",
                    "join",
                    "leave",
                    "limits",
                    "map",
                    "maps",
                    "players",
                    "state",
                    "team",
                    "teams",
                    "transition"
            );

    private final SpacetimeEnginePlugin plugin;

    public SpacetimeCommand(
            SpacetimeEnginePlugin plugin
    ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendPluginInformation(sender);
            return true;
        }

        return switch (
                args[0].toLowerCase(Locale.ROOT)
        ) {
            case "arena" ->
                    showArena(sender, args);

            case "arenas" ->
                    listArenas(sender);

            case "create" ->
                    createArena(sender, args);

            case "join" ->
                    joinArena(sender, args);

            case "leave" ->
                    leaveArena(sender, args);

            case "limits" ->
                    manageArenaLimits(sender, args);

            case "map" ->
                    showMap(sender, args);

            case "maps" ->
                    listMaps(sender);

            case "players" ->
                    showArenaPlayers(sender, args);

            case "state" ->
                    showArenaState(sender, args);

            case "team" ->
                    showTeam(sender, args);

            case "teams" ->
                    listTeams(sender, args);

            case "transition" ->
                    transitionArena(sender, args);

            default -> {
                sendUsage(sender, label);
                yield true;
            }
        };
    }

    private void sendPluginInformation(
            CommandSender sender
    ) {
        sender.sendMessage(
                Component.text(
                        "Spacetime Engine",
                        NamedTextColor.GOLD
                )
        );

        sender.sendMessage(
                Component.text(
                        "Version: "
                                + plugin.getPluginMeta()
                                .getVersion(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Status: online and ready "
                                + "for development.",
                        NamedTextColor.GREEN
                )
        );
    }

    private boolean listArenas(
            CommandSender sender
    ) {
        ArenaManager arenaManager =
                plugin.getArenaManager();

        sender.sendMessage(
                Component.text(
                        "Registered arenas: "
                                + arenaManager.size(),
                        NamedTextColor.GOLD
                )
        );

        for (Arena arena : arenaManager.getArenas()) {
            sender.sendMessage(
                    Component.text(
                            "- "
                                    + arena.getId()
                                    + " ["
                                    + arena.getState()
                                    + "] players: "
                                    + arena.getMinPlayers()
                                    + "-"
                                    + arena.getMaxPlayers(),
                            NamedTextColor.GRAY
                    )
            );
        }

        return true;
    }

    private boolean showArena(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime arena <arena>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena;

        try {
            arena = plugin.getArenaManager()
                    .findArena(args[1])
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        if (arena == null) {
            sender.sendMessage(
                    Component.text(
                            "Arena not found: " + args[1],
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Arena: " + arena.getId(),
                        NamedTextColor.GOLD
                )
        );

        sender.sendMessage(
                Component.text(
                        "State: " + arena.getState(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Players: "
                                + arena.getPlayerCount()
                                + "/"
                                + arena.getMaxPlayers()
                                + " | minimum: "
                                + arena.getMinPlayers(),
                        NamedTextColor.GRAY
                )
        );

        if (arena.getMapId().isEmpty()) {
            sender.sendMessage(
                    Component.text(
                            "Map: unassigned",
                            NamedTextColor.DARK_GRAY
                    )
            );

            return true;
        }

        String mapId = arena.getMapId().orElseThrow();

        GameMapDefinition map =
                plugin.getGameMapManager()
                        .findMap(mapId)
                        .orElse(null);

        if (map == null) {
            sender.sendMessage(
                    Component.text(
                            "Map unavailable: " + mapId,
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Map: " + map.getDisplayName(),
                        NamedTextColor.AQUA
                )
        );

        sender.sendMessage(
                Component.text(
                        "Map ID: " + map.getId(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "World: " + map.getWorldName(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Mode: " + map.getMode(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Teams: " + map.getTeamCount(),
                        NamedTextColor.GRAY
                )
        );

        return true;
    }

    private boolean createArena(
            CommandSender sender,
            String[] args
    ) {
        if (!hasAdminPermission(sender)) {
            return true;
        }

        if (
                args.length != 2
                        && args.length != 4
                        && args.length != 5
        ) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime create "
                                    + "<arena> [min] [max] [map]",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        try {
            Arena arena;

            if (args.length == 2) {
                arena = plugin.getArenaManager()
                        .createArena(args[1]);
            } else {
                Integer minPlayers = parseInteger(
                        sender,
                        args[2],
                        "minimum players"
                );

                Integer maxPlayers = parseInteger(
                        sender,
                        args[3],
                        "maximum players"
                );

                if (
                        minPlayers == null
                                || maxPlayers == null
                ) {
                    return true;
                }

                if (args.length == 5) {
                    GameMapDefinition map =
                            plugin.getGameMapManager()
                                    .findMap(args[4])
                                    .orElse(null);

                    if (map == null) {
                        sender.sendMessage(
                                Component.text(
                                        "Map not found: "
                                                + args[4],
                                        NamedTextColor.RED
                                )
                        );

                        return true;
                    }

                    arena = plugin.getArenaManager()
                            .createArena(
                                    args[1],
                                    minPlayers,
                                    maxPlayers,
                                    map.getId()
                            );
                } else {
                    arena = plugin.getArenaManager()
                            .createArena(
                                    args[1],
                                    minPlayers,
                                    maxPlayers
                            );
                }
            }

            if (!plugin.saveArenas()) {
                sender.sendMessage(
                        Component.text(
                                "Arena created in memory, "
                                        + "but could not be saved: "
                                        + arena.getId(),
                                NamedTextColor.RED
                        )
                );

                return true;
            }

            String mapInformation =
                    arena.getMapId()
                            .map(
                                    mapId ->
                                            " | map: " + mapId
                            )
                            .orElse("");

            sender.sendMessage(
                    Component.text(
                            "Arena created and saved: "
                                    + arena.getId()
                                    + mapInformation,
                            NamedTextColor.GREEN
                    )
            );
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );
        }

        return true;
    }

    private boolean joinArena(
            CommandSender sender,
            String[] args
    ) {
        Player player = requirePlayer(sender);

        if (player == null) {
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime join <arena>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        ArenaJoinResult result =
                plugin.getArenaPlayerManager()
                        .joinArena(
                                player.getUniqueId(),
                                arena
                        );

        switch (result) {
            case SUCCESS ->
                    sender.sendMessage(
                            Component.text(
                                    "Joined arena: "
                                            + arena.getId()
                                            + " ("
                                            + arena.getPlayerCount()
                                            + "/"
                                            + arena.getMaxPlayers()
                                            + ")",
                                    NamedTextColor.GREEN
                            )
                    );

            case ALREADY_IN_ARENA ->
                    sender.sendMessage(
                            Component.text(
                                    "You are already in an arena.",
                                    NamedTextColor.RED
                            )
                    );

            case ARENA_FULL ->
                    sender.sendMessage(
                            Component.text(
                                    "Arena is full: "
                                            + arena.getId(),
                                    NamedTextColor.RED
                            )
                    );

            case ARENA_NOT_ACCEPTING_PLAYERS ->
                    sender.sendMessage(
                            Component.text(
                                    "Arena is not accepting players: "
                                            + arena.getId(),
                                    NamedTextColor.RED
                            )
                    );

            case TEAM_ASSIGNMENT_UNAVAILABLE ->
                    sender.sendMessage(
                            Component.text(
                                    "Arena teams are unavailable: "
                                            + arena.getId(),
                                    NamedTextColor.RED
                            )
                    );
        }

        return true;
    }

    private boolean leaveArena(
            CommandSender sender,
            String[] args
    ) {
        Player player = requirePlayer(sender);

        if (player == null) {
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime leave",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = plugin.getArenaPlayerManager()
                .leaveArena(player.getUniqueId())
                .orElse(null);

        if (arena == null) {
            sender.sendMessage(
                    Component.text(
                            "You are not in an arena.",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Left arena: " + arena.getId(),
                        NamedTextColor.GREEN
                )
        );

        return true;
    }

    private boolean showArenaPlayers(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime players <arena>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Arena "
                                + arena.getId()
                                + " players: "
                                + arena.getPlayerCount()
                                + "/"
                                + arena.getMaxPlayers(),
                        NamedTextColor.AQUA
                )
        );

        return true;
    }

    private boolean listMaps(
            CommandSender sender
    ) {
        sender.sendMessage(
                Component.text(
                        "Registered maps: "
                                + plugin.getGameMapManager().size(),
                        NamedTextColor.GOLD
                )
        );

        for (
                GameMapDefinition map
                : plugin.getGameMapManager().getMaps()
        ) {
            sender.sendMessage(
                    Component.text(
                            "- "
                                    + map.getId()
                                    + " | "
                                    + map.getDisplayName()
                                    + " | teams: "
                                    + map.getTeamCount(),
                            NamedTextColor.GRAY
                    )
            );
        }

        return true;
    }

    private boolean showMap(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime map <map>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        GameMapDefinition map;

        try {
            map = plugin.getGameMapManager()
                    .findMap(args[1])
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        if (map == null) {
            sender.sendMessage(
                    Component.text(
                            "Map not found: " + args[1],
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        sender.sendMessage(
                Component.text(
                        map.getDisplayName(),
                        NamedTextColor.GOLD
                )
        );

        sender.sendMessage(
                Component.text(
                        "ID: " + map.getId(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "World: " + map.getWorldName(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Mode: " + map.getMode(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Teams: " + map.getTeamCount(),
                        NamedTextColor.AQUA
                )
        );

        sender.sendMessage(
                Component.text(
                        "Diamond generators: "
                                + map.getDiamondGenerators().size(),
                        NamedTextColor.AQUA
                )
        );

        sender.sendMessage(
                Component.text(
                        "Emerald generators: "
                                + map.getEmeraldGenerators().size(),
                        NamedTextColor.AQUA
                )
        );

        MapPoint center = map.getCenter();

        sender.sendMessage(
                Component.text(
                        "Center: "
                                + center.x()
                                + ", "
                                + center.y()
                                + ", "
                                + center.z(),
                        NamedTextColor.GRAY
                )
        );

        Location resolvedCenter =
                plugin.getMapLocationResolver()
                        .resolve(map, center)
                        .orElse(null);

        if (resolvedCenter == null) {
            sender.sendMessage(
                    Component.text(
                            "World status: unloaded",
                            NamedTextColor.YELLOW
                    )
            );

            sender.sendMessage(
                    Component.text(
                            "Resolved center: unavailable",
                            NamedTextColor.YELLOW
                    )
            );
        } else {
            sender.sendMessage(
                    Component.text(
                            "World status: loaded",
                            NamedTextColor.GREEN
                    )
            );

            sender.sendMessage(
                    Component.text(
                            "Resolved center: "
                                    + resolvedCenter.getX()
                                    + ", "
                                    + resolvedCenter.getY()
                                    + ", "
                                    + resolvedCenter.getZ()
                                    + " | yaw: "
                                    + resolvedCenter.getYaw()
                                    + " | pitch: "
                                    + resolvedCenter.getPitch(),
                            NamedTextColor.GRAY
                    )
            );
        }

        return true;
    }

    private boolean manageArenaLimits(
            CommandSender sender,
            String[] args
    ) {
        if (
                args.length != 2
                        && args.length != 4
        ) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime limits "
                                    + "<arena> [min] [max]",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        if (args.length == 2) {
            sender.sendMessage(
                    Component.text(
                            "Arena: " + arena.getId(),
                            NamedTextColor.GRAY
                    )
            );

            sender.sendMessage(
                    Component.text(
                            "Player limits: "
                                    + arena.getMinPlayers()
                                    + "-"
                                    + arena.getMaxPlayers(),
                            NamedTextColor.AQUA
                    )
            );

            return true;
        }

        if (!hasAdminPermission(sender)) {
            return true;
        }

        Integer minPlayers = parseInteger(
                sender,
                args[2],
                "minimum players"
        );

        Integer maxPlayers = parseInteger(
                sender,
                args[3],
                "maximum players"
        );

        if (
                minPlayers == null
                        || maxPlayers == null
        ) {
            return true;
        }

        try {
            arena.updatePlayerLimits(
                    minPlayers,
                    maxPlayers
            );

            if (!plugin.saveArenas()) {
                sender.sendMessage(
                        Component.text(
                                "Limits changed in memory, "
                                        + "but could not be saved.",
                                NamedTextColor.RED
                        )
                );

                return true;
            }

            sender.sendMessage(
                    Component.text(
                            "Player limits updated: "
                                    + arena.getId()
                                    + " "
                                    + minPlayers
                                    + "-"
                                    + maxPlayers,
                            NamedTextColor.GREEN
                    )
            );
        } catch (
                IllegalArgumentException
                        | IllegalStateException exception
        ) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );
        }

        return true;
    }

    private boolean listTeams(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime teams <arena>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        if (arena.getMapId().isEmpty()) {
            sender.sendMessage(
                    Component.text(
                            "Arena has no map assigned: "
                                    + arena.getId(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        String mapId =
                arena.getMapId().orElseThrow();

        GameMapDefinition map =
                plugin.getGameMapManager()
                        .findMap(mapId)
                        .orElse(null);

        if (map == null) {
            sender.sendMessage(
                    Component.text(
                            "Map unavailable: " + mapId,
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        var arenaTeams =
                plugin.getArenaTeamManager()
                        .getTeams(arena);

        sender.sendMessage(
                Component.text(
                        "Runtime teams for "
                                + arena.getId()
                                + " / "
                                + map.getDisplayName()
                                + ": "
                                + arenaTeams.size(),
                        NamedTextColor.GOLD
                )
        );

        for (ArenaTeam team : arenaTeams) {
            sender.sendMessage(
                    Component.text(
                            "- "
                                    + team.getId()
                                    + " | "
                                    + team.getDefinition()
                                            .getDisplayName()
                                    + " | players: "
                                    + team.getPlayerCount(),
                            NamedTextColor.GRAY
                    )
            );
        }

        return true;
    }

    private boolean showTeam(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 3) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime team "
                                    + "<arena> <team>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        if (arena.getMapId().isEmpty()) {
            sender.sendMessage(
                    Component.text(
                            "Arena has no map assigned: "
                                    + arena.getId(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        String mapId =
                arena.getMapId().orElseThrow();

        GameMapDefinition map =
                plugin.getGameMapManager()
                        .findMap(mapId)
                        .orElse(null);

        if (map == null) {
            sender.sendMessage(
                    Component.text(
                            "Map unavailable: " + mapId,
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        ArenaTeam team;

        try {
            team = plugin.getArenaTeamManager()
                    .findTeam(
                            arena,
                            args[2]
                    )
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        if (team == null) {
            sender.sendMessage(
                    Component.text(
                            "Team not found: " + args[2],
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        MapPoint base =
                team.getDefinition()
                        .getBaseAnchor();

        sender.sendMessage(
                Component.text(
                        "Team: "
                                + team.getDefinition()
                                    .getDisplayName(),
                        NamedTextColor.GOLD
                )
        );

        sender.sendMessage(
                Component.text(
                        "ID: " + team.getId(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Arena: " + team.getArenaId(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Players: "
                                + team.getPlayerCount(),
                        NamedTextColor.AQUA
                )
        );

        sender.sendMessage(
                Component.text(
                        "Map: " + map.getDisplayName(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Base anchor: "
                                + base.x()
                                + ", "
                                + base.y()
                                + ", "
                                + base.z(),
                        NamedTextColor.GRAY
                )
        );

        MapPoint spawn =
                team.getDefinition()
                        .getSpawn()
                        .orElse(null);

        if (spawn == null) {
            sender.sendMessage(
                    Component.text(
                            "Spawn: unconfigured",
                            NamedTextColor.YELLOW
                    )
            );
        } else {
            sender.sendMessage(
                    Component.text(
                            "Spawn: "
                                    + spawn.x()
                                    + ", "
                                    + spawn.y()
                                    + ", "
                                    + spawn.z()
                                    + " | yaw: "
                                    + spawn.yaw()
                                    + " | pitch: "
                                    + spawn.pitch(),
                            NamedTextColor.GRAY
                    )
            );
        }

        return true;
    }

    private boolean showArenaState(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime state <arena>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Arena: " + arena.getId(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "State: " + arena.getState(),
                        NamedTextColor.AQUA
                )
        );

        return true;
    }

    private boolean transitionArena(
            CommandSender sender,
            String[] args
    ) {
        if (!hasAdminPermission(sender)) {
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime transition "
                                    + "<arena> <state>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = findArenaOrNotify(
                sender,
                args[1]
        );

        if (arena == null) {
            return true;
        }

        ArenaState nextState;

        try {
            nextState = ArenaState.valueOf(
                    args[2].toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            "Unknown arena state: " + args[2],
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        ArenaState previousState =
                arena.getState();

        try {
            arena.transitionTo(nextState);
        } catch (IllegalStateException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        sender.sendMessage(
                Component.text(
                        "Arena "
                                + arena.getId()
                                + " transitioned: "
                                + previousState
                                + " -> "
                                + nextState,
                        NamedTextColor.GREEN
                )
        );

        return true;
    }

    private @Nullable Arena findArenaOrNotify(
            CommandSender sender,
            String arenaId
    ) {
        Arena arena;

        try {
            arena = plugin.getArenaManager()
                    .findArena(arenaId)
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            exception.getMessage(),
                            NamedTextColor.RED
                    )
            );

            return null;
        }

        if (arena == null) {
            sender.sendMessage(
                    Component.text(
                            "Arena not found: " + arenaId,
                            NamedTextColor.RED
                    )
            );
        }

        return arena;
    }

    private @Nullable Integer parseInteger(
            CommandSender sender,
            String value,
            String fieldName
    ) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            sender.sendMessage(
                    Component.text(
                            "Invalid " + fieldName + ": " + value,
                            NamedTextColor.RED
                    )
            );

            return null;
        }
    }

    private @Nullable Player requirePlayer(
            CommandSender sender
    ) {
        if (sender instanceof Player player) {
            return player;
        }

        sender.sendMessage(
                Component.text(
                        "This command can only be used by a player.",
                        NamedTextColor.RED
                )
        );

        return null;
    }

    private boolean hasAdminPermission(
            CommandSender sender
    ) {
        if (
                sender.hasPermission(
                        "spacetime.admin"
                )
        ) {
            return true;
        }

        sender.sendMessage(
                Component.text(
                        "You do not have permission "
                                + "to manage arenas.",
                        NamedTextColor.RED
                )
        );

        return false;
    }

    private void sendUsage(
            CommandSender sender,
            String label
    ) {
        sender.sendMessage(
                Component.text(
                        "Usage: /"
                                + label
                                + " [arena|arenas|create|join|leave|limits|map|maps|"
                                + "players|state|team|teams|transition]",
                        NamedTextColor.YELLOW
                )
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filterSuggestions(
                    SUBCOMMANDS,
                    args[0]
            );
        }

        if (
                args.length == 2
                        && (
                        args[0].equalsIgnoreCase("team")
                                || args[0].equalsIgnoreCase("teams")
                )
        ) {
            return filterSuggestions(
                    plugin.getArenaManager().getArenaIds(),
                    args[1]
            );
        }

        if (
                args.length == 3
                        && args[0].equalsIgnoreCase("team")
        ) {
            try {
                Arena arena =
                        plugin.getArenaManager()
                                .findArena(args[1])
                                .orElse(null);

                if (
                        arena == null
                                || arena.getMapId().isEmpty()
                ) {
                    return List.of();
                }

                GameMapDefinition map =
                        plugin.getGameMapManager()
                                .findMap(
                                        arena.getMapId()
                                                .orElseThrow()
                                )
                                .orElse(null);

                if (map == null) {
                    return List.of();
                }

                return filterSuggestions(
                        map.getTeamIds(),
                        args[2]
                );
            } catch (IllegalArgumentException exception) {
                return List.of();
            }
        }

        if (
                args.length == 2
                        && args[0].equalsIgnoreCase("arena")
        ) {
            return filterSuggestions(
                    plugin.getArenaManager().getArenaIds(),
                    args[1]
            );
        }

        if (
                args.length == 2
                        && args[0].equalsIgnoreCase("map")
        ) {
            return filterSuggestions(
                    plugin.getGameMapManager().getMapIds(),
                    args[1]
            );
        }

        if (
                args.length == 2
                        && (
                        args[0].equalsIgnoreCase("join")
                                || args[0].equalsIgnoreCase("limits")
                                || args[0].equalsIgnoreCase("players")
                                || args[0].equalsIgnoreCase("state")
                                || args[0].equalsIgnoreCase(
                                "transition"
                        )
                )
        ) {
            return filterSuggestions(
                    plugin.getArenaManager()
                            .getArenaIds(),
                    args[1]
            );
        }

        if (
                args.length == 3
                        && args[0].equalsIgnoreCase(
                        "transition"
                )
        ) {
            List<String> states =
                    Arrays.stream(ArenaState.values())
                            .map(state ->
                                    state.name()
                                            .toLowerCase(
                                                    Locale.ROOT
                                            )
                            )
                            .toList();

            return filterSuggestions(
                    states,
                    args[2]
            );
        }

        return List.of();
    }

    private List<String> filterSuggestions(
            List<String> suggestions,
            String input
    ) {
        String normalizedInput =
                input.toLowerCase(Locale.ROOT);

        return suggestions.stream()
                .filter(suggestion ->
                        suggestion.startsWith(
                                normalizedInput
                        )
                )
                .toList();
    }
}
