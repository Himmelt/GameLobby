package org.soraworld.lobby.command;

import org.bukkit.entity.Player;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.command.Sub;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.command.Tab;
import org.soraworld.violet.command.TabExecutor;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;
import org.soraworld.violet.util.ListUtils;

@Command(name = "lobby", usage = "/lobby open|close|info|finish|join|quit|list")
public final class CommandLobby {

    @Inject
    private LobbyManager manager;

    @Sub(perm = "admin", usage = "/lobby open <game_name>")
    public final SubExecutor open = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryOpenGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Tab(path = "open")
    public final TabExecutor tab_open = (cmd, sender, args) -> ListUtils.getMatchListIgnoreCase(args.first(), manager.getLobbies());

    @Sub(perm = "admin", usage = "/lobby close <game_name>")
    public final SubExecutor close = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryCloseGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Tab(path = "close")
    public final TabExecutor tab_close = (cmd, sender, args) -> ListUtils.getMatchListIgnoreCase(args.first(), manager.getLobbies());

    @Sub(perm = "admin", usage = "/lobby info <game_name>")
    public final SubExecutor info = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.showInfo(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Tab(path = "info")
    public final TabExecutor tab_info = (cmd, sender, args) -> ListUtils.getMatchListIgnoreCase(args.first(), manager.getLobbies());

    @Sub(perm = "admin", usage = "/lobby finish <game_name>")
    public final SubExecutor finish = (cmd, sender, args) -> {
        if (args.notEmpty()) {
            manager.tryForceFinishGame(sender, args.first());
        } else cmd.sendUsage(sender);
    };

    @Tab(path = "finish")
    public final TabExecutor tab_finish = (cmd, sender, args) -> ListUtils.getMatchListIgnoreCase(args.first(), manager.getLobbies());

    @Sub(onlyPlayer = true, usage = "/lobby join <game_name>")
    public final SubExecutor<Player> join = (cmd, player, args) -> {
        if (args.notEmpty()) {
            manager.tryJoinGame(player, args.first());
        } else cmd.sendUsage(player);
    };

    @Tab(path = "join")
    public final TabExecutor tab_join = (cmd, sender, args) -> ListUtils.getMatchListIgnoreCase(args.first(), manager.getLobbies());

    @Sub(onlyPlayer = true, usage = "/lobby quit")
    public final SubExecutor<Player> quit = (cmd, player, args) -> manager.tryQuitGame(player);

    @Sub(usage = "/lobby list")
    public final SubExecutor list = (cmd, sender, args) -> manager.listGames(sender);
}
