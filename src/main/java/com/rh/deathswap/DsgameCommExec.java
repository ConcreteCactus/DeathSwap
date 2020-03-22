package com.rh.deathswap;

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
        if(!dsgame.addPlayer((Player) commandSender)){
            ((Player) commandSender).sendRawMessage("Sorry, the game is already running on this server or you already have joined.");
        }else{
            ((Player) commandSender).sendRawMessage("Joined deathswap game.");
            if(first){((Player) commandSender).sendRawMessage("Only you can start the game with '/dsgame-start'"); }
        }

    }

    public void onLeave(CommandSender commandSender){
        if(!dsgame.removePlayer((Player) commandSender)){
            ((Player) commandSender).sendRawMessage("You have already left, or you are not in the lobby anymore, if you want to forfeit the game, run '/dsgame-stop'");
        }else{
            ((Player) commandSender).sendRawMessage("You have left the lobby.");
        }
    }

    public void onStart(CommandSender commandSender){

        if(!dsgame.startGame((Player) commandSender)){
            ((Player) commandSender).sendRawMessage("You can't start this game, only the player who joined first can or the game is already running.");
        }
    }

    public void onStop(CommandSender commandSender){
        if(!dsgame.playerForfeit((Player) commandSender)){
            ((Player) commandSender).sendRawMessage("You can't stop playing, maybe because you are not playing at all.");
        }else{
            ((Player) commandSender).sendRawMessage("You stopped playing and forfeited the game.");
        }
    }
}
