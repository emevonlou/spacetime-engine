package io.github.emevonlou.spacetimeengine.listener;

import io.github.emevonlou.spacetimeengine.arena.ArenaPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public final class PlayerConnectionListener
        implements Listener {

    private final ArenaPlayerManager arenaPlayerManager;

    public PlayerConnectionListener(
            ArenaPlayerManager arenaPlayerManager
    ) {
        this.arenaPlayerManager =
                Objects.requireNonNull(
                        arenaPlayerManager,
                        "ArenaPlayerManager cannot be null."
                );
    }

    @EventHandler
    public void onPlayerQuit(
            PlayerQuitEvent event
    ) {
        arenaPlayerManager.leaveArena(
                event.getPlayer().getUniqueId()
        );
    }
}
