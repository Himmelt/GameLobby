package org.soraworld.lobby.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.soraworld.lobby.core.AbstractLobby;
import org.soraworld.lobby.manager.LobbyManager;

public class EventListener implements Listener {

    private final LobbyManager manager;

    public EventListener(LobbyManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearGame(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        AbstractLobby lobby = manager.getPlayerLobby(event.getEntity().getUniqueId());
        if (lobby != null) {
            lobby.onPlayerDeath(player);
        }
    }
}
