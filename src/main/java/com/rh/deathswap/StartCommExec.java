package com.rh.deathswap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommExec implements CommandExecutor {

    DSgame dsgame;

    public StartCommExec(DSgame game){
        dsgame = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if((commandSender instanceof Player) && args.length == 0) {

            if(!dsgame.startGame((Player) commandSender)){
                ((Player) commandSender).sendRawMessage("You can't start this game, only the player who joined first can or the game is already running.");
            }

            return true;
        }else{
            return false;
        }
    }
}
