package com.rh.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    DSgame game;

    @Override
    public void onEnable() {

        game = new DSgame(this);

        this.getCommand("dsgame").setExecutor(new DsgameCommExec(game));
        this.getCommand("dsgame").setTabCompleter(new DsgameCommExec(game));

        getServer().getPluginManager().registerEvents(new EventListener(game, this), this);

        getLogger().info("Deathswap plugin enabled");
    }

    @Override
    public void onDisable() {
        game.endGame();
        getLogger().info("Deathswap plugin disabled");
    }
}
