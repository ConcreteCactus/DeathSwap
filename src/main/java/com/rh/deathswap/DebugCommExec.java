package com.rh.deathswap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommExec implements CommandExecutor {

    DSgame dsgame;

    public DebugCommExec(DSgame game){
        dsgame = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if((commandSender instanceof Player) && args.length == 1) {



            return true;
        }else{
            return false;
        }
    }
}