package org.soraworld.lobby.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

    public boolean shouldStart(long time, List<Player> players, Map<Location, List<Player>> factions) {
        return time >= 400;
    }

    public boolean shouldFinish(long time, List<Player> players, Map<Location, List<Player>> factions) {
        return time >= 1000;
    }

    public boolean shouldClose(long time) {
        return time >= 2000;
    }

    public boolean allowJoin(Player player) {
        return true;
    }

    public boolean allowStart(Player player) {
        return true;
    }

    public boolean allowQuit(Player player) {
        return getState() != GameState.START;
    }

    public void onOpen() {
        Bukkit.broadcastMessage("Example Lobby Opened !");
    }

    public void onStart() {
        Bukkit.broadcastMessage("Example Lobby Started !");
    }

    public void onUpdate(long time) {
        if (time % 100 == 0) Bukkit.broadcastMessage("Example Lobby has opened " + time + " ticks !");
    }

    public void onFinish() {
        Bukkit.broadcastMessage("Example Game Finished !");
    }

    public void onClose() {
        Bukkit.broadcastMessage("Example Lobby Closed !");
    }
}
