package org.soraworld.lobby.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LobbyData {
    public long gameLife = 0;
    public long lobbyLife = 0;
    public GameState state = GameState.CLOSE;
    public ArrayList<Player> players = new ArrayList<>();
    public HashMap<Location, List<Player>> factions = new HashMap<>();
}
