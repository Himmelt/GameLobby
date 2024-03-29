package org.soraworld.lobby;

import org.soraworld.lobby.core.IGameLobby;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.plugin.SpigotPlugin;

@Inject
public final class GameLobby extends SpigotPlugin<LobbyManager> {

    @Inject
    private static LobbyManager theManager;

    @Override
    public String getId() {
        return "lobby";
    }

    @Override
    public void beforeDisable() {
        manager.unregisterAllLobbies();
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
     * @param lobby 大厅
     */
    public static void registerGameLobby(IGameLobby lobby) {
        if (theManager != null) theManager.registerGameLobby(lobby);
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
