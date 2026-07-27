package io.github.emevonlou.spacetimeengine.command;

import io.github.emevonlou.spacetimeengine.SpacetimeEnginePlugin;
import io.github.emevonlou.spacetimeengine.arena.Arena;
import io.github.emevonlou.spacetimeengine.arena.ArenaState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class SpacetimeCommand
        implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "state",
            "transition"
    );

    private final SpacetimeEnginePlugin plugin;

    public SpacetimeCommand(SpacetimeEnginePlugin plugin) {
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

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "state" -> showArenaState(sender);
            case "transition" -> transitionArena(sender, args);
            default -> {
                sendUsage(sender, label);
                yield true;
            }
        };
    }

    private void sendPluginInformation(CommandSender sender) {
        sender.sendMessage(
                Component.text("Spacetime Engine", NamedTextColor.GOLD)
        );

        sender.sendMessage(
                Component.text(
                        "Version: " + plugin.getPluginMeta().getVersion(),
                        NamedTextColor.GRAY
                )
        );

        sender.sendMessage(
                Component.text(
                        "Status: online and ready for development.",
                        NamedTextColor.GREEN
                )
        );
    }

    private boolean showArenaState(CommandSender sender) {
        Arena arena = plugin.getDevelopmentArena();

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
        if (!sender.hasPermission("spacetime.admin")) {
            sender.sendMessage(
                    Component.text(
                            "You do not have permission to manage arenas.",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /spacetime transition <state>",
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        ArenaState nextState;

        try {
            nextState = ArenaState.valueOf(
                    args[1].toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(
                    Component.text(
                            "Unknown arena state: " + args[1],
                            NamedTextColor.RED
                    )
            );

            return true;
        }

        Arena arena = plugin.getDevelopmentArena();
        ArenaState previousState = arena.getState();

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
                        "Arena transitioned: "
                                + previousState
                                + " -> "
                                + nextState,
                        NamedTextColor.GREEN
                )
        );

        return true;
    }

    private void sendUsage(
            CommandSender sender,
            String label
    ) {
        sender.sendMessage(
                Component.text(
                        "Usage: /" + label + " [state|transition <state>]",
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
            return filterSuggestions(SUBCOMMANDS, args[0]);
        }

        if (
                args.length == 2
                        && args[0].equalsIgnoreCase("transition")
        ) {
            List<String> states = Arrays.stream(ArenaState.values())
                    .map(state -> state.name().toLowerCase(Locale.ROOT))
                    .toList();

            return filterSuggestions(states, args[1]);
        }

        return List.of();
    }

    private List<String> filterSuggestions(
            List<String> suggestions,
            String input
    ) {
        String normalizedInput = input.toLowerCase(Locale.ROOT);

        return suggestions.stream()
                .filter(suggestion ->
                        suggestion.startsWith(normalizedInput)
                )
                .toList();
    }
}
