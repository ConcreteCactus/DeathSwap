package com.rh.deathswap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommExec implements CommandExecutor {

    DSgame dsgame;

    public JoinCommExec(DSgame game){
        dsgame = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if((commandSender instanceof Player) && args.length == 0) {

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

            return true;
        }else{
            return false;
        }
    }
}
