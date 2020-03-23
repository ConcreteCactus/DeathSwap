package com.rh.deathswap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
// This is the CommandExecutor and TabCompleter class of the /dsgame command
public class DsgameCommExec  implements CommandExecutor, TabCompleter {

    DSgame dsgame;

    public DsgameCommExec(DSgame game){
        dsgame = game;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length != 1){
            return false;
        }

        switch(args[0]){
            case "join":
                onJoin(commandSender);
                break;
            case "leave":
                onLeave(commandSender);
                break;
            case "start":
                onStart(commandSender);
                break;
            case "stop":
                onStop(commandSender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return (args.length == 1)? Arrays.asList("join", "leave", "start", "stop") : null;
    }

    public void onJoin(CommandSender commandSender){

        boolean first = false;
        if(dsgame.getPlayerCount() < 1){
            first = true;
        }
        int ret = dsgame.addPlayer((Player) commandSender);
        switch(ret){
            case 0:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "Joined deathswap game.");
                break;
            case -1: // Already in lobby
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are already in the lobby.");
                break;
            case -2: // Game has already started
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "The game has started, sry...");
        }

        if(first){((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "Only you can start the game with '/dsgame start'"); }

    }

    public void onLeave(CommandSender commandSender){
        int ret = dsgame.removePlayer((Player) commandSender);
        switch(ret){
            case 0:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You have left the lobby.");
                break;
            case -1: // Game has started
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are not in the lobby anymore, if you want to forfeit the game, run '/dsgame stop'");
                break;
            case -2: // Player is not in lobby
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are not in the lobby");
                break;
        }
    }

    public void onStart(CommandSender commandSender){
        int ret = dsgame.startGame((Player) commandSender);
        switch(ret){
            case 0:
                break;
            case -1: // The number of online players in lobby is less than 2
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "There is not enough players in the lobby.");
                break;
            case -2: // commandSender is not the first player in the lobby
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You can't start the game.");
                break;
            case -3: // The game has already started
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "The game is already running.");
        }
    }

    public void onStop(CommandSender commandSender){

        int ret = dsgame.playerForfeit((Player) commandSender);
        switch(ret){
            case 0:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You stopped playing and forfeited the game.");
                break;
            case -1: // Player is still in lobby
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You can't stop playing, you are in the lobby. If you want to leave the lobby, run /dsgame leave");
                break;
            case -2: // Player is not in lobby
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You have already stopped playing.");
                break;
        }

    }
}
