package org.soraworld.lobby.core;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.inject.Inject;

import java.util.*;

/**
 * 游戏大厅接口.
 */
@Inject
public abstract class AbstractLobby {

    private long gameLife = 0;
    private long lobbyLife = 0;
    private GameState state = GameState.CLOSE;
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Location, List<Player>> factions = new HashMap<>();

    @Inject
    private static LobbyManager manager;

    /**
     * 游戏大厅显示名.
     *
     * @return 显示名 string
     */
    public abstract String display();

    /**
     * 获取游戏大厅的中心位置.
     *
     * @return 中心位置 center
     */
    public abstract Location getCenter();

    /**
     * 获取大厅半径.
     *
     * @return 大厅半径 radius
     */
    public abstract int getRadius();

    /**
     * 获取大厅半径类型.
     *
     * @return 大厅半径类型 r type
     */
    public abstract RType getRType();

    /**
     * 获取传送映射信息.
     * 大厅集会点 -&gt; 游戏传送点
     * 此方法应该只读，不得进行添加删除操作!
     * **** 务必返回非空映射 ****
     *
     * @return 传送映射信息 transfer
     */
    public abstract Map<Location, Location> getTransfer();

    /**
     * 是否开启大厅.
     * 这里可以根据游戏时间，或系统时间，定时或周期性开启。
     * 比如每天晚上 8 点开启。
     * 游戏必须处于 {@link GameState#CLOSE} 或 {@link GameState#FINISH} 状态.
     *
     * @return 是否开启大厅
     */
    public abstract boolean shouldOpen();

    /**
     * 是否开始游戏.
     * 若开始,则传送玩家到 {@link AbstractLobby#getTransfer} 设定的目标位置
     *
     * @param lobbyLife 从大厅启动到当前的时间，单位 tick
     * @param players   当前大厅玩家列表
     * @param factions  每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldStart(long lobbyLife, final List<Player> players, final Map<Location, List<Player>> factions);

    /**
     * 是否结束游戏.
     * 若结束,则传送玩家到 {@link AbstractLobby#getCenter} 设定的位置
     *
     * @param lobbyLife 从大厅启动到当前的时间，单位 tick
     * @param gameLife  游戏开始至此刻的时间
     * @param players   当前游戏玩家列表
     * @param factions  每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldFinish(long lobbyLife, long gameLife, final List<Player> players, final Map<Location, List<Player>> factions);

    /**
     * 是否关闭游戏大厅.
     * 游戏必须处于 {@link GameState#OPEN} 或 {@link GameState#FINISH} 状态.
     *
     * @param time 从大厅启动到当前的时间，单位 tick
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldClose(long time);

    /**
     * 当玩家尝试加入大厅时.
     * 如果返回 false 则拒绝玩家加入.
     *
     * @param player 玩家
     * @return 是否允许加入 boolean
     */
    public abstract boolean onPlayerJoin(Player player);

    /**
     * 当玩家尝试被传送游戏传送点时.
     * 如果返回 null 则拒绝玩家传送.
     *
     * @param player 玩家
     * @param origin 原始传送位置
     * @return 最终传送位置
     */
    public abstract Location onPlayerStart(Player player, Location origin);

    /**
     * 当玩家主动退出时.
     * 如果返回 false 则拒绝玩家退出.
     *
     * @param player 玩家
     * @return 是否允许退出 boolean
     */
    public abstract boolean onPlayerQuit(Player player);

    /**
     * 大厅开启时.
     */
    public abstract void onOpen();

    /**
     * 游戏开始时.
     */
    public abstract void onStart();

    /**
     * 游戏周期更新时.
     *
     * @param lobbyLife 大厅开启至今的时长(单位: tick)
     * @param gameLife  游戏开始至此刻的时间
     */
    public abstract void onUpdate(long lobbyLife, long gameLife);

    /**
     * 游戏结束时.
     */
    public abstract void onFinish();

    /**
     * 大厅关闭时.
     */
    public abstract void onClose();

    /**
     * 在玩家死亡时.
     *
     * @param player 玩家
     */
    public abstract void onPlayerDeath(Player player);

    /**
     * 开启游戏大厅.
     *
     * @param sender the sender
     */
    public final void openLobby(CommandSender sender) {
        if (state.canOpen()) {
            onOpen();
            lobbyLife = 0;
            gameLife = 0;
            state = GameState.OPEN;
            if (sender != null) manager.sendKey(sender, "openLobby", display());
        } else if (sender != null) {
            manager.sendKey(sender, "cantOpenLobby", display(), state);
        }
    }

    /**
     * 关闭游戏大厅.
     *
     * @param sender the sender
     */
    public final void closeLobby(CommandSender sender) {
        if (state.canClose()) {
            onClose();
            if (state != GameState.FINISH) {
                players.forEach(manager::clearGame);
                players.clear();
                factions.clear();
            }
            state = GameState.CLOSE;
            if (sender != null) manager.sendKey(sender, "closeLobby", display());
        } else if (sender != null) {
            manager.sendKey(sender, "stateCantClose", display(), state);
        }
    }

    /**
     * 游戏周期更新.
     */
    public final void update() {
        if (shouldOpen()) openLobby(null);
        if (state != GameState.CLOSE) {
            lobbyLife += manager.updateFrequency();
            if (state == GameState.OPEN) {
                checkLobby();
                if (shouldStart(lobbyLife, players, factions)) {
                    onStart();
                    state = GameState.START;
                    Map<Location, Location> transfer = getTransfer();
                    factions.forEach((fac, players) -> {
                        Location target = transfer.get(fac);
                        players.forEach(player -> {
                            Location loc = onPlayerStart(player, target);
                            if (loc != null) player.teleport(loc);
                        });
                    });
                }
            }
            if (state == GameState.START) gameLife += manager.updateFrequency();
            onUpdate(lobbyLife, gameLife);
            if (state.canFinish() && shouldFinish(lobbyLife, gameLife, players, factions)) {
                finishGame();
            }
            if (state.canClose() && shouldClose(lobbyLife)) {
                closeLobby(null);
            }
        }
    }

    /**
     * (强制)结束游戏，并传送所有玩家回大厅中心.
     */
    public final void finishGame() {
        onFinish();
        players.forEach(player -> {
            manager.clearGame(player);
            player.teleport(getCenter());
        });
        players.clear();
        factions.clear();
        state = GameState.FINISH;
    }

    private void checkLobby() {
        players.clear();
        factions.clear();
        Location center = getCenter();
        double cX = center.getX(), cY = center.getY(), cZ = center.getZ();
        int radius = getRadius();
        RType type = getRType();
        Set<Location> locations = getTransfer().keySet();
        center.getWorld().getPlayers().forEach(player -> {
            if (manager.isJoined(player, this)) {
                Location pLoc = player.getLocation();
                double pX = pLoc.getX(), pY = pLoc.getY(), pZ = pLoc.getZ();
                switch (type) {
                    case CUBE: {
                        double minX = cX - radius, minY = cY - radius, minZ = cZ - radius;
                        double maxX = cX + radius, maxY = cY + radius, maxZ = cZ + radius;
                        if (pX >= minX && pX <= maxX && pY >= minY && pY <= maxY && pZ >= minZ && pZ <= maxZ) {
                            Location fac = getNearestLoc(locations, pLoc);
                            factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            players.add(player);
                        }
                        break;
                    }
                    case CUBOID_COLUMN: {
                        double minX = cX - radius, minZ = cZ - radius;
                        double maxX = cX + radius, maxZ = cZ + radius;
                        if (pX >= minX && pX <= maxX && pZ >= minZ && pZ <= maxZ) {
                            Location fac = getNearestLoc(locations, pLoc);
                            factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            players.add(player);
                        }
                        break;
                    }
                    case SPHERE: {
                        if ((cX - pX) * (cX - pX) + (cY - pY) * (cY - pY) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                            Location fac = getNearestLoc(locations, pLoc);
                            factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            players.add(player);
                        }
                        break;
                    }
                    case CIRCLE_COLUMN: {
                        if ((cX - pX) * (cX - pX) + (cZ - pZ) * (cZ - pZ) <= radius * radius) {
                            Location fac = getNearestLoc(locations, pLoc);
                            factions.computeIfAbsent(fac, location -> new ArrayList<>()).add(player);
                            players.add(player);
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取游戏大厅状态.
     *
     * @return 状态
     */
    public final GameState getState() {
        return state;
    }

    /**
     * 获取大厅最近一次开启至此刻的时间.
     *
     * @return 开启时长
     */
    public final long getLobbyLife() {
        return lobbyLife;
    }

    /**
     * 获取游戏开始至此刻的时间.
     *
     * @return 游戏开始时长
     */
    public final long getGameLife() {
        return gameLife;
    }

    /**
     * 踢出玩家.
     *
     * @param player 玩家
     */
    public final void kickPlayer(Player player) {
        players.remove(player);
        factions.forEach((location, ps) -> ps.remove(player));
        manager.clearGame(player);
    }

    /**
     * 传送玩家到游戏大厅.
     *
     * @param player 目标玩家
     */
    public final void tpPlayerToLobby(Player player) {
        player.teleport(getCenter());
    }

    private static Location getNearestLoc(Collection<Location> locations, Location source) {
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
