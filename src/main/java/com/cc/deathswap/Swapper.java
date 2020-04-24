package com.cc.deathswap;

import org.bukkit.Bukkit;

//This class handles the swapping
public class Swapper implements Runnable {

    DSgame game;

    public Swapper(DSgame g){
        game = g;
    }

    @Override
    public void run() {
        if(game.swapping){
            game.swap();
            long time = game.getRandSwapTime();
            Bukkit.getServer().getScheduler().runTaskLater(game.jplugin, new DSgame.CountDownTimer(game), time - 10 * 20);
            Bukkit.getServer().getScheduler().runTaskLater(game.jplugin, this, time);
        }
    }
}
