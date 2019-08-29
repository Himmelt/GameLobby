package org.soraworld.lobby.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.lobby.GameLobby;
import org.soraworld.lobby.event.*;

import java.util.*;

/**
 * 游戏大厅接口.
 */
public interface IGameLobby {

    /**
     * 游戏大厅 id.
     *
     * @return id string
     */
    @NotNull
    String id();

    /**
     * 计划任务执行周期.
     *
     * @return 周期 int
     */
    int cycle();

    /**
     * 游戏大厅显示名.
     *
     * @return 显示名 string
     */
    @NotNull
    String display();

    /**
     * 获取游戏大厅的中心位置.
     *
     * @return 中心位置 center
     */
    Location getCenter();

    /**
     * 获取大厅半径.
     *
     * @return 大厅半径 radius
     */
    int getRadius();

    /**
     * 获取大厅半径类型.
     *
     * @return 大厅半径类型 r type
     */
    @NotNull
    RType getRType();

    /**
     * 获取传送映射信息.
     * 大厅集会点 -&gt; 游戏传送点
     * 此方法应该只读，不得进行添加删除操作!
     * **** 务必返回非空映射 ****
     *
     * @return 传送映射信息 transfer
     */
    @NotNull
    Map<Location, Location> getTransfer();

    /**
     * 检查大厅开启的准备条件。
     * 自动开启与命令开启都会检查。
     *
     * @return 是否准备就绪 boolean
     */
    boolean checkPrepare();

    /**
     * 是否开启大厅。
     * 这里可以根据游戏时间，或系统时间，定时或周期性开启。
     * 比如每天晚上 8 点开启。
     * 游戏必须处于 {@link GameState#CLOSE} 或 {@link GameState#FINISH} 状态。
     *
     * @return 是否开启大厅 boolean
     */
    boolean shouldOpen();

    /**
     * 是否开始游戏.
     * 若开始,则传送玩家到 {@link IGameLobby#getTransfer} 设定的目标位置
     *
     * @param lobbyLife 从大厅启动到当前的时间，单位 tick
     * @param players   当前大厅玩家列表
     * @param factions  每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    boolean shouldStart(long lobbyLife, @NotNull final List<Player> players, @NotNull final Map<Location, @NotNull List<Player>> factions);

    /**
     * 是否结束游戏.
     * 若结束,则传送玩家到 {@link IGameLobby#getCenter} 设定的位置
     *
     * @param lobbyLife 从大厅启动到当前的时间，单位 tick
     * @param gameLife  游戏开始至此刻的时间
     * @param players   当前游戏玩家列表
     * @param factions  每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    boolean shouldFinish(long lobbyLife, long gameLife, @NotNull final List<Player> players, @NotNull final Map<Location, List<Player>> factions);

    /**
     * 是否关闭游戏大厅.
     * 游戏必须处于 {@link GameState#OPEN} 或 {@link GameState#FINISH} 状态.
     *
     * @param time 从大厅启动到当前的时间，单位 tick
     * @return 是否开始传送 boolean
     */
    boolean shouldClose(long time);

    /**
     * 当玩家尝试加入大厅时.
     * 如果返回 false 则拒绝玩家加入.
     *
     * @param player 玩家
     * @return 是否允许加入 boolean
     */
    boolean onPlayerJoin(@NotNull Player player);

    /**
     * 当玩家尝试被传送游戏传送点时.
     * 如果返回 null 则拒绝玩家传送.
     *
     * @param player 玩家
     * @param origin 原始传送位置
     * @return 最终传送位置 location
     */
    @Nullable
    Location onPlayerStart(@NotNull Player player, @NotNull Location origin);

    /**
     * 当玩家主动退出时.
     * 如果返回 false 则拒绝玩家退出.
     *
     * @param player 玩家
     * @return 是否允许退出 boolean
     */
    boolean onPlayerQuit(@NotNull Player player);

    /**
     * 大厅开启时.
     */
    void onOpen();

    /**
     * 游戏开始时.
     */
    void onStart();

    /**
     * 游戏周期更新时.
     *
     * @param lobbyLife 大厅开启至今的时长(单位: tick)
     * @param gameLife  游戏开始至此刻的时间
     */
    void onUpdate(long lobbyLife, long gameLife);

    /**
     * 游戏结束时.
     */
    void onFinish();

    /**
     * 大厅关闭时.
     */
    void onClose();

    /**
     * 在玩家死亡时.
     *
     * @param player 玩家
     */
    void onPlayerDeath(@NotNull Player player);

    /**
     * 额外信息，执行 info 指令时输出.
     *
     * @return 额外信息列表
     */
    @Nullable
    List<String> extraInfo();

    /**
     * 向 执行者 发送消息.
     *
     * @param sender  执行者
     * @param message 消息
     */
    void send(CommandSender sender, String message);

    /**
     * 向 执行者 发送消息键.
     *
     * @param sender 执行者
     * @param key    键
     * @param args   参数
     */
    void sendKey(CommandSender sender, String key, Object... args);

    /**
     * 开启游戏大厅.
     *
     * @param sender 命令执行者
     */
    default void openLobby(@Nullable CommandSender sender) {
        if (checkPrepare()) {
            LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
            if (data.state.canOpen()) {
                Bukkit.getPluginManager().callEvent(new LobbyOpenEvent(this));
                onOpen();
                data.lobbyLife = 0;
                data.gameLife = 0;
                data.state = GameState.OPEN;
                if (sender != null) GameLobby.getLobbyManager().sendKey(sender, "openLobby", display());
            } else if (sender != null) {
                GameLobby.getLobbyManager().sendKey(sender, "cantOpenLobby", display(), data.state);
            }
        } else {
            if (sender != null) GameLobby.getLobbyManager().sendKey(sender, "notPrepare", display());
            else GameLobby.getLobbyManager().consoleKey("notPrepare", display());
        }
    }

    /**
     * 关闭游戏大厅.
     *
     * @param sender the sender
     */
    default void closeLobby(@Nullable CommandSender sender) {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        if (data.state.canClose()) {
            Bukkit.getPluginManager().callEvent(new LobbyCloseEvent(this));
            onClose();
            if (data.state != GameState.FINISH) {
                data.players.forEach(GameLobby.getLobbyManager()::clearGame);
                data.players.clear();
                data.factions.clear();
            }
            data.state = GameState.CLOSE;
            if (sender != null) GameLobby.getLobbyManager().sendKey(sender, "closeLobby", display());
        } else if (sender != null) {
            GameLobby.getLobbyManager().sendKey(sender, "stateCantClose", display(), data.state);
        }
    }

    /**
     * 游戏周期更新.
     */
    default void update() {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        if (shouldOpen()) openLobby(null);
        if (data.state != GameState.CLOSE) {
            data.lobbyLife += cycle();
            if (data.state == GameState.OPEN) {
                checkLobby();
                if (shouldStart(data.lobbyLife, data.players, data.factions)) {
                    Bukkit.getPluginManager().callEvent(new LobbyStartEvent(this));
                    onStart();
                    data.state = GameState.START;
                    Map<Location, Location> transfer = getTransfer();
                    data.factions.forEach((fac, players) -> {
                        Location target = transfer.get(fac);
                        if (target != null) players.forEach(player -> {
                            Location loc = onPlayerStart(player, target);
                            if (loc != null) player.teleport(loc);
                        });
                    });
                }
            }
            if (data.state == GameState.START) data.gameLife += cycle();
            Bukkit.getPluginManager().callEvent(new LobbyUpdateEvent(this));
            onUpdate(data.lobbyLife, data.gameLife);
            if (data.state.canFinish() && shouldFinish(data.lobbyLife, data.gameLife, data.players, data.factions)) {
                finishGame();
            }
            if (data.state.canClose() && shouldClose(data.lobbyLife)) {
                closeLobby(null);
            }
        }
    }

    /**
     * (强制)结束游戏，并传送所有玩家回大厅中心.
     */
    default void finishGame() {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        Bukkit.getPluginManager().callEvent(new LobbyFinishEvent(this));
        onFinish();
        // GameLobby.getLobbyManager().clearGame(player);
        data.players.forEach(this::tpPlayerToLobby);
        // data.players.clear();
        // data.factions.clear();
        data.state = GameState.FINISH;
    }

    /**
     * 检查大厅人员状态.
     */
    default void checkLobby() {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        data.players.clear();
        data.factions.clear();
        Location center = getCenter();
        if (center == null) return;
        double cX = center.getX(), cY = center.getY(), cZ = center.getZ();
        int radius = getRadius();
        RType type = getRType();
        Set<Location> locations = getTransfer().keySet();
        center.getWorld().getPlayers().forEach(player -> {
            if (GameLobby.getLobbyManager().isJoined(player, this)) {
                Location pLoc = player.getLocation();
                double pX = pLoc.getX(), pY = pLoc.getY(), pZ = pLoc.getZ();
                switch (type) {
                    case CUBE: {
                        double minX = cX - radius, minY = cY - radius, minZ = cZ - radius;
                        double maxX = cX + radius, maxY = cY + radius, maxZ = cZ + radius;
                        if (pX >= minX && pX <= maxX && pY >= minY && pY <= maxY && pZ >= minZ && pZ <= maxZ) {
                            Location fac = getNearestLoc(locations, pLoc);
                            data.factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            data.players.add(player);
                        }
                        break;
                    }
                    case CUBOID_COLUMN: {
                        double minX = cX - radius, minZ = cZ - radius;
                        double maxX = cX + radius, maxZ = cZ + radius;
                        if (pX >= minX && pX <= maxX && pZ >= minZ && pZ <= maxZ) {
                            Location fac = getNearestLoc(locations, pLoc);
                            data.factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            data.players.add(player);
                        }
                        break;
                    }
                    case SPHERE: {
                        if ((cX - pX) * (cX - pX) + (cY - pY) * (cY - pY) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                            Location fac = getNearestLoc(locations, pLoc);
                            data.factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            data.players.add(player);
                        }
                        break;
                    }
                    case CIRCLE_COLUMN: {
                        if ((cX - pX) * (cX - pX) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                            Location fac = getNearestLoc(locations, pLoc);
                            data.factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            data.players.add(player);
                        }
                    }
                }
            }
        });
    }

    default boolean inLobbyRange(@NotNull Player player) {
        Location center = getCenter();
        if (center != null) {
            int radius = getRadius();
            double cX = center.getX(), cY = center.getY(), cZ = center.getZ();
            Location pLoc = player.getLocation();
            double pX = pLoc.getX(), pY = pLoc.getY(), pZ = pLoc.getZ();
            switch (getRType()) {
                case CUBE: {
                    double minX = cX - radius, minY = cY - radius, minZ = cZ - radius;
                    double maxX = cX + radius, maxY = cY + radius, maxZ = cZ + radius;
                    if (pX >= minX && pX <= maxX && pY >= minY && pY <= maxY && pZ >= minZ && pZ <= maxZ) {
                        return true;
                    }
                    break;
                }
                case CUBOID_COLUMN: {
                    double minX = cX - radius, minZ = cZ - radius;
                    double maxX = cX + radius, maxZ = cZ + radius;
                    if (pX >= minX && pX <= maxX && pZ >= minZ && pZ <= maxZ) {
                        return true;
                    }
                    break;
                }
                case SPHERE: {
                    if ((cX - pX) * (cX - pX) + (cY - pY) * (cY - pY) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                        return true;
                    }
                    break;
                }
                case CIRCLE_COLUMN: {
                    if ((cX - pX) * (cX - pX) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取游戏大厅状态.
     *
     * @return 状态 state
     */
    default GameState getState() {
        return GameLobby.getLobbyManager().getLobbyData(this).state;
    }

    /**
     * 获取大厅最近一次开启至此刻的时间.
     *
     * @return 开启时长 lobby life
     */
    default long getLobbyLife() {
        return GameLobby.getLobbyManager().getLobbyData(this).lobbyLife;
    }

    /**
     * 获取游戏开始至此刻的时间.
     *
     * @return 游戏开始时长 game life
     */
    default long getGameLife() {
        return GameLobby.getLobbyManager().getLobbyData(this).gameLife;
    }

    /**
     * 踢出玩家.
     *
     * @param player 玩家
     */
    default void kickPlayer(@NotNull Player player) {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        data.players.remove(player);
        data.factions.forEach((location, ps) -> ps.remove(player));
        GameLobby.getLobbyManager().clearGame(player);
    }

    /**
     * 传送玩家到游戏大厅.
     *
     * @param player 目标玩家
     */
    default void tpPlayerToLobby(@NotNull Player player) {
        Location center = getCenter();
        if (center != null) {
            center = center.clone();
            center.setPitch(player.getLocation().getPitch());
            center.setYaw(player.getLocation().getYaw());
            player.teleport(center);
        }
    }

    /**
     * 向游戏内玩家广播消息.
     *
     * @param message 消息
     */
    default void gameBroadcast(@NotNull String message) {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        data.players.forEach(player -> send(player, message));
    }

    /**
     * 向游戏内玩家广播消息.
     *
     * @param key  键
     * @param args 参数
     */
    default void gameBroadcastKey(@NotNull String key, Object... args) {
        LobbyData data = GameLobby.getLobbyManager().getLobbyData(this);
        data.players.forEach(player -> sendKey(player, key, args));
    }

    /**
     * 获取最近的位置.
     *
     * @param locations 位置集合
     * @param source    源
     * @return 最近的位置 nearest loc
     */
    static Location getNearestLoc(@NotNull Collection<Location> locations, @NotNull Location source) {
        double min = Double.MAX_VALUE;
        Location target = null;
        for (Location loc : locations) {
            double distance = source.distanceSquared(loc);
            if (distance < min) {
                min = distance;
                target = loc;
            }
        }
        return target;
    }
}
