package org.soraworld.lobby;

import org.bukkit.event.Listener;
import org.soraworld.lobby.command.CommandLobby;
import org.soraworld.lobby.core.AbstractLobby;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.command.SpigotBaseSubs;
import org.soraworld.violet.command.SpigotCommand;
import org.soraworld.violet.manager.SpigotManager;
import org.soraworld.violet.plugin.SpigotPlugin;

import java.nio.file.Path;
import java.util.List;

public final class GameLobby extends SpigotPlugin {

    private static LobbyManager theManager;

    @Override
    public String getId() {
        return "lobby";
    }

    @Override
    protected SpigotManager registerManager(Path path) {
        theManager = new LobbyManager(this, path);
        return theManager;
    }

    @Override
    protected List<Listener> registerListeners() {
        return null;
    }

    @Override
    protected void registerCommands() {
        SpigotCommand command = new SpigotCommand(getId(), null, false, theManager);
        command.extractSub(SpigotBaseSubs.class);
        command.extractSub(CommandLobby.class);
        register(this, command);
    }

    /**
     * 获取游戏大厅管理器.
     *
     * @return 游戏大厅管理器
     */
    public static LobbyManager getLobbyManager() {
        return theManager;
    }

    /**
     * 注册游戏大厅.
     *
     * @param name  名称
     * @param lobby 大厅
     */
    public void registerGameLobby(String name, AbstractLobby lobby) {
        if (theManager != null) theManager.registerGameLobby(name, lobby);
    }

    /**
     * 取消注册游戏大厅.
     *
     * @param name 名称
     */
    public void unregisterGameLobby(String name) {
        if (theManager != null) theManager.unregisterGameLobby(name);
    }
}
