package org.soraworld.lobby.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.soraworld.lobby.GameLobby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExampleLobby implements IGameLobby {

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

    @Override
    public @NotNull String id() {
        return "example";
    }

    @Override
    public int cycle() {
        return 10;
    }

    @NotNull
    public String display() {
        return "Example Lobby";
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return 20;
    }

    @NotNull
    public RType getRType() {
        return RType.CUBOID_COLUMN;
    }

    @NotNull
    public Map<Location, Location> getTransfer() {
        return transfer;
    }

    @Override
    public boolean checkPrepare() {
        return true;
    }

    public boolean shouldOpen() {
        return false;
    }

    public boolean shouldStart(long lobbyLife, @NotNull List<Player> players, @NotNull Map<Location, List<Player>> factions) {
        return lobbyLife >= 400;
    }

    public boolean shouldFinish(long lobbyLife, long gameLife, @NotNull List<Player> players, @NotNull Map<Location, List<Player>> factions) {
        return lobbyLife >= 1000 || gameLife >= 800;
    }

    public boolean shouldClose(long time) {
        return time >= 2000;
    }

    public boolean onPlayerJoin(@NotNull Player player) {
        return true;
    }

    @Nullable
    public Location onPlayerStart(@NotNull Player player, @NotNull Location origin) {
        return player.getName().equals("Don'tTransfer") ? null : origin;
    }

    public boolean onPlayerQuit(@NotNull Player player) {
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

    public void onPlayerDeath(@NotNull Player player) {
        kickPlayer(player);
    }

    @Override
    public @Nullable List<String> extraInfo() {
        return null;
    }

    @Override
    public void send(CommandSender sender, String message) {
        GameLobby.getLobbyManager().send(sender, message);
    }

    @Override
    public void sendKey(CommandSender sender, String key, Object... args) {
        GameLobby.getLobbyManager().sendKey(sender, key, args);
    }
}
