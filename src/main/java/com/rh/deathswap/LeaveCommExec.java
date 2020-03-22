package com.rh.deathswap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommExec implements CommandExecutor {

    DSgame dsgame;

    public LeaveCommExec(DSgame game){
        dsgame = game;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if((commandSender instanceof Player) && args.length == 0) {

            if(!dsgame.removePlayer((Player) commandSender)){
                ((Player) commandSender).sendRawMessage("You have already left, or you are not in the lobby anymore, if you want to forfeit the game, run '/dsgame-stop'");
            }else{
                ((Player) commandSender).sendRawMessage("You have left the lobby.");
            }

            return true;
        }else{
            return false;
        }
    }
}
