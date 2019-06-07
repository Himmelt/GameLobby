package org.soraworld.lobby.core;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.lobby.manager.LobbyManager;

import java.util.*;

/**
 * 游戏大厅接口.
 */
public abstract class AbstractLobby {

    private long life = 0;
    private GameState state = GameState.CLOSE;
    private final LobbyManager manager;
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Location, List<Player>> factions = new HashMap<>();

    /**
     * 游戏大厅构造器.
     *
     * @param manager 管理器
     */
    public AbstractLobby(LobbyManager manager) {
        this.manager = manager;
    }

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
     * 大厅集会点 -> 游戏传送点
     * 此方法应该只读，不得进行添加删除操作!
     * **** 务必返回非空映射 ****
     *
     * @return 传送映射信息 transfer
     */
    public abstract Map<Location, Location> getTransfer();

    /**
     * 是否开始游戏.
     * 若开始,则传送玩家到{@link AbstractLobby#getTransfer();}设定的目标位置
     *
     * @param time     从大厅启动到当前的时间，单位 update
     * @param players  当前大厅玩家列表
     * @param factions 每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldStart(long time, final List<Player> players, final Map<Location, List<Player>> factions);

    /**
     * 是否结束游戏.
     * 若结束,则传送玩家到{@link AbstractLobby#getCenter();}设定的位置
     *
     * @param time     从大厅启动到当前的时间，单位 update
     * @param players  当前游戏玩家列表
     * @param factions 每个阵营的玩家列表
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldFinish(long time, final List<Player> players, final Map<Location, List<Player>> factions);

    /**
     * 是否关闭游戏大厅.
     * 游戏必须处于 {@link GameState#OPEN} 或 {@link GameState#FINISH} 状态.
     *
     * @param time 从大厅启动到当前的时间，单位 tick
     * @return 是否开始传送 boolean
     */
    public abstract boolean shouldClose(long time);

    /**
     * 是否允许加入大厅.
     *
     * @param player 玩家
     * @return 是否允许加入 boolean
     */
    public abstract boolean allowJoin(Player player);

    /**
     * 是否允许开始游戏.
     *
     * @param player 玩家
     * @return 是否允许开始 boolean
     */
    public abstract boolean allowStart(Player player);

    /**
     * 是否允许退出游戏.
     *
     * @param player 玩家
     * @return 是否允许退出 boolean
     */
    public abstract boolean allowQuit(Player player);

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
     * @param time 大厅开启至今的时长(单位: tick)
     */
    public abstract void onUpdate(long time);

    /**
     * 游戏结束时.
     */
    public abstract void onFinish();

    /**
     * 大厅关闭时.
     */
    public abstract void onClose();

    /**
     * 开启游戏大厅.
     *
     * @param sender the sender
     */
    public final void openLobby(CommandSender sender) {
        if (state.canOpen()) {
            onOpen();
            life = 0;
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
        if (state != GameState.CLOSE) {
            life += manager.updateFrequency();
            if (state == GameState.OPEN) {
                checkLobby();
                if (shouldStart(life, players, factions)) {
                    onStart();
                    state = GameState.START;
                    Map<Location, Location> transfer = getTransfer();
                    factions.forEach((fac, players) -> {
                        Location target = transfer.get(fac);
                        players.forEach(player -> {
                            if (allowStart(player)) player.teleport(target);
                        });
                    });
                }
            }
            onUpdate(life);
            if (state.canFinish() && shouldFinish(life, players, factions)) {
                finishGame();
            }
            if (state.canClose() && shouldClose(life)) {
                closeLobby(null);
            }
        }
    }

    /**
     * (强制)结束游戏，并传送所有玩家回大厅中心.
     */
    public void finishGame() {
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
    public long getLife() {
        return life;
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
