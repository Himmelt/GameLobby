package org.soraworld.lobby.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.soraworld.lobby.core.AbstractLobby;

public abstract class LobbyEvent extends Event {

    @NotNull
    private final AbstractLobby lobby;

    protected LobbyEvent(@NotNull AbstractLobby lobby) {
        this.lobby = lobby;
    }

    @NotNull
    public AbstractLobby getLobby() {
        return lobby;
    }
}
