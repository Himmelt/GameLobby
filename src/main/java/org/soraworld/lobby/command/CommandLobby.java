package org.soraworld.lobby.command;

import org.bukkit.entity.Player;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.command.Sub;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;

@Command(name = "lobby", perm = "admin", usage = "usage.lobby")
public final class CommandLobby {

    @Inject
    private LobbyManager manager;

    @Sub(perm = "admin", usage = "/lobby open <game_name>")
    public final SubExecutor open = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryOpenGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Sub(perm = "admin", usage = "/lobby close <game_name>")
    public final SubExecutor close = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryCloseGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Sub(perm = "admin", usage = "/lobby finish <game_name>")
    public final SubExecutor finish = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryForceFinishGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Sub(onlyPlayer = true, usage = "/lobby join <game_name>")
    public final SubExecutor<Player> join = (cmd, player, args) -> {
        if (args.notEmpty()) {
            manager.tryJoinGame(player, args.first());
        } else cmd.sendUsage(player);
    };

    @Sub(onlyPlayer = true, usage = "/lobby quit")
    public final SubExecutor<Player> quit = (cmd, player, args) -> manager.tryQuitGame(player);

    @Sub(usage = "/lobby list")
    public final SubExecutor list = (cmd, sender, args) -> manager.listGames(sender);
}
