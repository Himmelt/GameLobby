package org.soraworld.lobby.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.soraworld.lobby.manager.LobbyManager;
import org.soraworld.violet.command.Args;
import org.soraworld.violet.command.SpigotCommand;
import org.soraworld.violet.command.Sub;

public final class CommandLobby {
    @Sub(perm = "admin", usage = "/lobby open <game_name>")
    public static void open(SpigotCommand self, CommandSender sender, Args args) {
        LobbyManager manager = (LobbyManager) self.manager;
        if (args.notEmpty()) {
            manager.tryOpenGame(sender, args.first());
        } else self.sendUsage(sender);
    }

    @Sub(perm = "admin", usage = "/lobby close <game_name>")
    public static void close(SpigotCommand self, CommandSender sender, Args args) {
        LobbyManager manager = (LobbyManager) self.manager;
        if (args.notEmpty()) {
            manager.tryCloseGame(sender, args.first());
        } else self.sendUsage(sender);
    }

    @Sub(perm = "admin", usage = "/lobby finish <game_name>")
    public static void finish(SpigotCommand self, CommandSender sender, Args args) {
        LobbyManager manager = (LobbyManager) self.manager;
        if (args.notEmpty()) {
            manager.tryForceFinishGame(sender, args.first());
        } else self.sendUsage(sender);
    }

    @Sub(onlyPlayer = true, usage = "/lobby join <game_name>")
    public static void join(SpigotCommand self, CommandSender sender, Args args) {
        Player player = (Player) sender;
        LobbyManager manager = (LobbyManager) self.manager;
        if (args.notEmpty()) {
            manager.tryJoinGame(player, args.first());
        } else self.sendUsage(player);
    }

    @Sub(onlyPlayer = true, usage = "/lobby quit")
    public static void quit(SpigotCommand self, CommandSender sender, Args args) {
        Player player = (Player) sender;
        LobbyManager manager = (LobbyManager) self.manager;
        manager.tryQuitGame(player);
    }

    @Sub(usage = "/lobby list")
    public static void list(SpigotCommand self, CommandSender sender, Args args) {
        LobbyManager manager = (LobbyManager) self.manager;
        manager.listGames(sender);
    }
}
