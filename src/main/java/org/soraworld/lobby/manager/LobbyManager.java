package org.soraworld.lobby.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.soraworld.hocon.node.Setting;
import org.soraworld.lobby.core.AbstractLobby;
import org.soraworld.lobby.core.ExampleLobby;
import org.soraworld.lobby.core.GameState;
import org.soraworld.violet.manager.SpigotManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.UUID;

public class LobbyManager extends SpigotManager {

    @Setting(comment = "comment.updateFrequency")
    private int updateFrequency = 20;
    private BukkitTask task;

    private HashMap<UUID, String> playerGames = new HashMap<>();
    private HashMap<String, AbstractLobby> registerLobbies = new HashMap<>();

    public LobbyManager(SpigotPlugin plugin, Path path) {
        super(plugin, path);
    }

    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    /**
     * 注册游戏大厅.
     *
     * @param name  名称
     * @param lobby 大厅
     */
    public void registerGameLobby(String name, AbstractLobby lobby) {
        if (registerLobbies.putIfAbsent(name, lobby) != null) {
            consoleKey("gameAlreadyExist", name);
        }
    }

    /**
     * 是否已注册游戏大厅.
     *
     * @param name 名称
     * @return 是否已注册
     */
    public boolean isRegistered(String name) {
        return registerLobbies.containsKey(name);
    }

    /**
     * 获取已注册游戏大厅.
     *
     * @param name 名称
     * @return 游戏大厅
     */
    public AbstractLobby getRegisterLobby(String name) {
        return registerLobbies.get(name);
    }

    /**
     * 取消注册游戏大厅.
     *
     * @param name 名称
     */
    public void unregisterGameLobby(String name) {
        registerLobbies.remove(name);
        consoleKey("gameRemoved", name);
    }

    public void tryOpenGame(CommandSender sender, String name) {
        AbstractLobby lobby = registerLobbies.get(name);
        if (lobby != null) lobby.openLobby(sender);
        else sendKey(sender, "gameNotExist", name);
    }

    public void tryCloseGame(CommandSender sender, String name) {
        AbstractLobby lobby = registerLobbies.get(name);
        if (lobby != null) lobby.closeLobby(sender);
        else sendKey(sender, "gameNotExist", name);
    }

    public void tryForceFinishGame(CommandSender sender, String name) {
        AbstractLobby lobby = registerLobbies.get(name);
        if (lobby != null) {
            if (lobby.getState() == GameState.START) {
                lobby.finishGame();
                sendKey(sender, "finishGame", lobby.display());
            } else sendKey(sender, "gameNotStart", lobby.display());
        } else {
            sendKey(sender, "gameNotExist", name);
        }
    }

    public void tryJoinGame(Player player, String game) {
        UUID uuid = player.getUniqueId();
        String current = playerGames.get(uuid);
        if (current != null && registerLobbies.containsKey(current)) {
            sendKey(player, "alreadyInGame", registerLobbies.get(current).display());
            return;
        }
        AbstractLobby lobby = registerLobbies.get(game);
        if (lobby != null) {
            switch (lobby.getState()) {
                case OPEN:
                case FINISH:
                    if (lobby.onPlayerJoin(player)) {
                        playerGames.put(uuid, game);
                        player.teleport(lobby.getCenter());
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

    public void listGames(CommandSender sender) {
        StringJoiner joiner = new StringJoiner(",");
        registerLobbies.keySet().forEach(joiner::add);
        sendKey(sender, "gameList", joiner.toString());
    }

    public void tryQuitGame(Player player) {
        UUID uuid = player.getUniqueId();
        String current = playerGames.get(uuid);
        if (current != null) {
            AbstractLobby lobby = registerLobbies.get(current);
            if (lobby != null) {
                if (lobby.onPlayerQuit(player)) {
                    playerGames.remove(uuid);
                    sendKey(player, "quitGame", lobby.display());
                } else sendKey(player, "gameRejectQuit", lobby.display());
            } else sendKey(player, "gameNotExist", current);
        } else sendKey(player, "notJoinAnyGame");
    }

    public int updateFrequency() {
        return updateFrequency;
    }

    public void clearGame(Player player) {
        playerGames.remove(player.getUniqueId());
    }

    @Override
    public void afterLoad() {
        if (!registerLobbies.containsKey("example")) {
            registerGameLobby("example", new ExampleLobby(new Location(Bukkit.getWorlds().get(0), 0, 100, 0)));
        }
        if (task != null) task.cancel();
        task = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> registerLobbies.values().forEach(AbstractLobby::update),
                updateFrequency, updateFrequency);
    }

    public boolean isJoined(Player player, AbstractLobby lobby) {
        return registerLobbies.get(playerGames.getOrDefault(player.getUniqueId(), "")) == lobby;
    }
}
