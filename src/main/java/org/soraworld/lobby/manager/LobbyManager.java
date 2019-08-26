package org.soraworld.lobby.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.soraworld.lobby.core.ExampleLobby;
import org.soraworld.lobby.core.GameState;
import org.soraworld.lobby.core.IGameLobby;
import org.soraworld.lobby.core.LobbyData;
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.*;

@MainManager
public class LobbyManager extends VManager {

    private HashMap<UUID, String> playerGames = new HashMap<>();
    private HashMap<String, IGameLobby> registerLobbies = new HashMap<>();
    private HashMap<IGameLobby, LobbyData> lobbyDataMap = new HashMap<>();

    public LobbyManager(SpigotPlugin plugin, Path path) {
        super(plugin, path);
    }

    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    /**
     * 注册游戏大厅.
     *
     * @param lobby 大厅
     */
    public void registerGameLobby(@NotNull IGameLobby lobby) {
        if (registerLobbies.putIfAbsent(lobby.id(), lobby) != null) {
            consoleKey("gameAlreadyExist", lobby.id());
        } else {
            LobbyData data = new LobbyData();
            data.task = Bukkit.getScheduler().runTaskTimer(plugin, lobby::update, lobby.cycle(), lobby.cycle());
            lobbyDataMap.put(lobby, data);
            consoleKey("gameRegisterSuccess", lobby.id());
        }
    }

    /**
     * 是否已注册游戏大厅.
     *
     * @param name 名称
     * @return 是否已注册
     */
    public boolean isRegistered(@NotNull String name) {
        return registerLobbies.containsKey(name);
    }

    /**
     * 获取已注册游戏大厅.
     *
     * @param name 名称
     * @return 游戏大厅
     */
    public IGameLobby getRegisterLobby(@NotNull String name) {
        return registerLobbies.get(name);
    }

    /**
     * 取消注册游戏大厅.
     *
     * @param name 名称
     */
    public void unregisterGameLobby(@NotNull String name) {
        IGameLobby lobby = registerLobbies.remove(name);
        if (lobby != null) {
            LobbyData data = lobbyDataMap.remove(lobby);
            if (data != null) {
                data.players.forEach(p -> playerGames.remove(p.getUniqueId()));
                data.players.clear();
                data.factions.clear();
                if (data.task != null) data.task.cancel();
            }
            consoleKey("gameRemoved", name);
        } else consoleKey("gameNotExist", name);
    }

    public IGameLobby getPlayerLobby(@NotNull UUID uuid) {
        return registerLobbies.get(playerGames.getOrDefault(uuid, ""));
    }

    public void tryOpenGame(@NotNull CommandSender sender, @NotNull String name) {
        IGameLobby lobby = registerLobbies.get(name);
        if (lobby != null) lobby.openLobby(sender);
        else sendKey(sender, "gameNotExist", name);
    }

    public void tryCloseGame(@NotNull CommandSender sender, @NotNull String name) {
        IGameLobby lobby = registerLobbies.get(name);
        if (lobby != null) lobby.closeLobby(sender);
        else sendKey(sender, "gameNotExist", name);
    }

    public void tryForceFinishGame(@NotNull CommandSender sender, @NotNull String name) {
        IGameLobby lobby = registerLobbies.get(name);
        if (lobby != null) {
            if (lobby.getState() == GameState.START) {
                lobby.finishGame();
                sendKey(sender, "finishGame", lobby.display());
            } else sendKey(sender, "gameNotStart", lobby.display());
        } else {
            sendKey(sender, "gameNotExist", name);
        }
    }

    public void tryJoinGame(@NotNull Player player, @NotNull String game) {
        UUID uuid = player.getUniqueId();
        String current = playerGames.get(uuid);
        if (current != null && registerLobbies.containsKey(current)) {
            sendKey(player, "alreadyInGame", registerLobbies.get(current).display());
            return;
        }
        IGameLobby lobby = registerLobbies.get(game);
        if (lobby != null) {
            switch (lobby.getState()) {
                case OPEN:
                case FINISH:
                    if (lobby.onPlayerJoin(player)) {
                        playerGames.put(uuid, game);
                        Location center = lobby.getCenter();
                        if (center != null) player.teleport(center);
                    } else sendKey(player, "gameRejectJoin", lobby.display());
                    break;
                case START:
                    sendKey(player, "gameStarted", lobby.display());
                    break;
                case CLOSE:
                    sendKey(player, "lobbyNotOpen", lobby.display());
            }
        } else sendKey(player, "gameNotExist", game);
    }

    public void listGames(@NotNull CommandSender sender) {
        StringJoiner joiner = new StringJoiner(",");
        registerLobbies.keySet().forEach(joiner::add);
        sendKey(sender, "gameList", joiner.toString());
    }

    public void tryQuitGame(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        String current = playerGames.get(uuid);
        if (current != null) {
            IGameLobby lobby = registerLobbies.get(current);
            if (lobby != null) {
                if (lobby.onPlayerQuit(player)) {
                    playerGames.remove(uuid);
                    sendKey(player, "quitGame", lobby.display());
                } else sendKey(player, "gameRejectQuit", lobby.display());
            } else sendKey(player, "gameNotExist", current);
        } else sendKey(player, "notJoinAnyGame");
    }

    public void clearGame(@NotNull Player player) {
        playerGames.remove(player.getUniqueId());
    }

    @Override
    public void afterLoad() {
        if (!registerLobbies.containsKey("example")) {
            registerGameLobby(new ExampleLobby(new Location(Bukkit.getWorlds().get(0), 0, 100, 0)));
        }
    }

    public boolean isJoined(@NotNull Player player, @NotNull IGameLobby lobby) {
        return registerLobbies.get(playerGames.getOrDefault(player.getUniqueId(), "")) == lobby;
    }

    public List<String> getLobbies() {
        return new ArrayList<>(registerLobbies.keySet());
    }

    @NotNull
    public LobbyData getLobbyData(@NotNull IGameLobby lobby) {
        return lobbyDataMap.computeIfAbsent(lobby, l -> new LobbyData());
    }

    public void showInfo(@NotNull CommandSender sender, @NotNull String game) {
        IGameLobby lobby = registerLobbies.get(game);
        if (lobby != null) {
            Location center = lobby.getCenter();
            String text = "{" + center.getWorld().getName() + "," + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ() + "}";
            sendKey(sender, "info.head");
            sendKey(sender, "info.display", lobby.display());
            sendKey(sender, "info.center", text);
            sendKey(sender, "info.state", lobby.getState().toString());
            sendKey(sender, "info.foot");
        }
    }

    public void unregisterAllLobbies() {
        List<String> ids = new ArrayList<>(registerLobbies.keySet());
        ids.forEach(this::unregisterGameLobby);
    }
}
