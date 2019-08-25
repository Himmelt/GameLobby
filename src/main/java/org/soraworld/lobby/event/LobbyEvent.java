package org.soraworld.lobby.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.soraworld.lobby.core.IGameLobby;

public abstract class LobbyEvent extends Event {

    @NotNull
    private final IGameLobby lobby;

    protected LobbyEvent(@NotNull IGameLobby lobby) {
        this.lobby = lobby;
    }

    @NotNull
    public IGameLobby getLobby() {
        return lobby;
    }
}
