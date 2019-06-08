package org.soraworld.lobby.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExampleLobby extends AbstractLobby {

    private final Location center;
    private final HashMap<Location, Location> transfer = new HashMap<>();

    public ExampleLobby(Location center) {
        this.center = center;
        Location loc1_1 = center.clone().add(5, 0, 0);
        Location loc1_2 = center.clone().add(5, 10, 0);
        Location loc2_1 = center.clone().add(-5, 0, 0);
        Location loc2_2 = center.clone().add(-5, 10, 0);
        Location loc3_1 = center.clone().add(0, 0, 5);
        Location loc3_2 = center.clone().add(0, 10, 5);
        Location loc4_1 = center.clone().add(0, 0, -5);
        Location loc4_2 = center.clone().add(0, 10, -5);
        transfer.put(loc1_1, loc1_2);
        transfer.put(loc2_1, loc2_2);
        transfer.put(loc3_1, loc3_2);
        transfer.put(loc4_1, loc4_2);
    }

    public String display() {
        return "Example Lobby";
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return 20;
    }

    public RType getRType() {
        return RType.CUBOID_COLUMN;
    }

    public Map<Location, Location> getTransfer() {
        return transfer;
    }

    public boolean shouldOpen() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 14;
    }

    public boolean shouldStart(long lobbyLife, List<Player> players, Map<Location, List<Player>> factions) {
        return lobbyLife >= 400;
    }

    public boolean shouldFinish(long lobbyLife, long gameLife, List<Player> players, Map<Location, List<Player>> factions) {
        return lobbyLife >= 1000 || gameLife >= 800;
    }

    public boolean shouldClose(long time) {
        return time >= 2000;
    }

    public boolean onPlayerJoin(Player player) {
        return true;
    }

    public Location onPlayerStart(Player player, Location origin) {
        return origin;
    }

    public boolean onPlayerQuit(Player player) {
        return getState() != GameState.START;
    }

    public void onOpen() {
        Bukkit.broadcastMessage("Example Lobby Opened !");
    }

    public void onStart() {
        Bukkit.broadcastMessage("Example Lobby Started !");
    }

    public void onUpdate(long lobbyLife, long gameLife) {
        if (lobbyLife % 100 == 0) Bukkit.broadcastMessage("Example Lobby has opened " + lobbyLife + " ticks !");
    }

    public void onFinish() {
        Bukkit.broadcastMessage("Example Game Finished !");
    }

    public void onClose() {
        Bukkit.broadcastMessage("Example Lobby Closed !");
    }

    public void onPlayerDeath(Player player) {
        kickPlayer(player);
    }
}
