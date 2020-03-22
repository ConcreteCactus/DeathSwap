package com.rh.deathswap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopCommExec implements CommandExecutor {

    DSgame dsgame;

    public StopCommExec(DSgame game){
        dsgame = game;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if((commandSender instanceof Player) && args.length == 0) {

            if(!dsgame.playerForfeit((Player) commandSender)){
                ((Player) commandSender).sendRawMessage("You can't stop playing, maybe because you are not playing at all.");
            }else{
                ((Player) commandSender).sendRawMessage("You stopped playing and forfeited the game.");

            }

            return true;
        }else{
            return false;
        }
    }
}
