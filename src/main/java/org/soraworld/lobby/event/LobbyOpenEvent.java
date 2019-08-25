package org.soraworld.lobby.event;

import org.bukkit.event.HandlerList;
import org.soraworld.lobby.core.AbstractLobby;

public class LobbyOpenEvent extends LobbyEvent {
    private static final HandlerList handlerList = new HandlerList();

    public LobbyOpenEvent(AbstractLobby lobby) {
        super(lobby);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
