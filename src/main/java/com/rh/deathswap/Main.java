package com.rh.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    DSgame game;

    @Override
    public void onEnable() {

        game = new DSgame(this);

        this.getCommand("dsgame-join").setExecutor(new JoinCommExec(game));
        this.getCommand("dsgame-start").setExecutor(new StartCommExec(game));
        this.getCommand("dsgame-stop").setExecutor(new StopCommExec(game));
        this.getCommand("dsgame-leave").setExecutor(new LeaveCommExec(game));
        this.getCommand("dsgame-debug").setExecutor(new DebugCommExec(game));

        getServer().getPluginManager().registerEvents(new EventListener(game, this), this);

        getLogger().info("Deathswap plugin enabled");
    }

    @Override
    public void onDisable() {
        game.endGame();
        getLogger().info("Deathswap plugin disabled");
    }
}
