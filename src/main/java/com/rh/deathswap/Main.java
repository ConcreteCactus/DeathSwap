package com.rh.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    DSgame game; //This is the main engine of the game

    @Override
    public void onEnable() {

        game = new DSgame(this);
        game.deleteTempWorld(); // tempworld is the separate world to host a deathswap game

        this.getCommand("dsgame").setExecutor(new DsgameCommExec(game));
        this.getCommand("dsgame").setTabCompleter(new DsgameCommExec(game));

        getServer().getPluginManager().registerEvents(new EventListener(game, this), this);

        getLogger().info("Deathswap plugin enabled");
    }

    @Override
    public void onDisable() {
        game.endGame(true); //Stops the game
        getLogger().info("Deathswap plugin disabled");
    }
}
