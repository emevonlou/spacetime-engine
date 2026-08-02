package io.github.emevonlou.spacetimeengine.command;

import io.github.emevonlou.spacetimeengine.SpacetimeEnginePlugin;
import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.arena.ArenaManager;
import io.github.emevonlou.spacetimeengine.arena.ArenaJoinResult;
import io.github.emevonlou.spacetimeengine.arena.ArenaState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
                    "arenas",
                    "create",
                    "join",
                    "leave",
                    "limits",
                    "players",
                    "state",
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

            case "players" ->
                    showArenaPlayers(sender, args);

            case "state" ->
                    showArenaState(sender, args);

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
        ) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime create "
                                    + "<arena> [min] [max]",
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

                arena = plugin.getArenaManager()
                        .createArena(
                                args[1],
                                minPlayers,
                                maxPlayers
                        );
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

            sender.sendMessage(
                    Component.text(
                            "Arena created and saved: "
                                    + arena.getId(),
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
                                + " [arenas|create|join|leave|limits|"
                                + "players|state|transition]",
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
