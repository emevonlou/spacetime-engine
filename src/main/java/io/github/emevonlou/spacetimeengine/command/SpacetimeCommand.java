package io.github.emevonlou.spacetimeengine.command;

import io.github.emevonlou.spacetimeengine.SpacetimeEnginePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SpacetimeCommand implements CommandExecutor {

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

        return true;
    }
}
