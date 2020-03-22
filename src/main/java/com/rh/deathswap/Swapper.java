package com.rh.deathswap;

import org.bukkit.Bukkit;

public class Swapper implements Runnable {

    DSgame game;

    public Swapper(DSgame g){
        game = g;
    }

    @Override
    public void run() {
        if(game.trunning){
            game.swap();
            Bukkit.getServer().getScheduler().runTaskLater(game.jplugin, this, (long)game.getRandSwapTime() * 20);
        }
    }
}
