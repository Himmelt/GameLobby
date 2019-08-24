package org.soraworld.lobby;

import org.soraworld.lobby.core.AbstractLobby;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.plugin.SpigotPlugin;

@Inject
public final class GameLobby extends SpigotPlugin {

    @Inject
    private static LobbyManager theManager;

    @Override
    public String getId() {
        return "lobby";
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
    public static void registerGameLobby(String name, AbstractLobby lobby) {
        if (theManager != null) theManager.registerGameLobby(name, lobby);
    }

    /**
     * 取消注册游戏大厅.
     *
     * @param name 名称
     */
    public static void unregisterGameLobby(String name) {
        if (theManager != null) theManager.unregisterGameLobby(name);
    }
}
