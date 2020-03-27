package com.cc.deathswap;

//This class's whole purpose is to call dSplayer.leave() in the next game tick
public class PlayerLeaveRunner implements Runnable {

    DSplayer dSplayer;

    public PlayerLeaveRunner(DSplayer dsp){
        dSplayer = dsp;
    }

    @Override
    public void run() {
        dSplayer.leave();
    }
}
