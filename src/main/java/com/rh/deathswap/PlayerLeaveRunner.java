package com.rh.deathswap;

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
