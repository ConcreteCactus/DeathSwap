package com.rh.deathswap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

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
        return Arrays.asList("join", "leave", "start", "stop");
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
            case -1:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are already in the lobby.");
                break;
            case -2:
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
            case -1:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are not in the lobby anymore, if you want to forfeit the game, run '/dsgame stop'");
                break;
            case -2:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You are not in the lobby");
                break;
        }
    }

    public void onStart(CommandSender commandSender){
        int ret = dsgame.startGame((Player) commandSender);
        switch(ret){
            case 0:
                break;
            case -1:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "There is not enough players in the lobby.");
                break;
            case -2:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You can't start the game.");
                break;
            case -3:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "The game is already running.");
        }
    }

    public void onStop(CommandSender commandSender){

        int ret = dsgame.playerForfeit((Player) commandSender);
        switch(ret){
            case 0:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You stopped playing and forfeited the game.");
                break;
            case -1:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You can't stop playing, you are in the lobby. If you want to leave the lobby, run /dsgame leave");
                break;
            case -2:
                ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + "You have already stopped playing.");
                break;
        }

    }
}
