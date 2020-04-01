package com.cc.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public class DSMainPlugin extends JavaPlugin {

    public static final double def_swaptimemax = 150.0;
    public static final double def_swaptimemin = 100.0;
    public static final String def_difficulty = "normal";
    public static final String def_tempworld_name = "dsgame-temporaryworld";

    DSgame dsgame; //This is the main engine of the game

    @Override
    public void onEnable() {

        dsgame = new DSgame(this);
        dsgame.deleteTempWorld(); // tempworld is the separate world to host a deathswap game
        loadConfig();

        getCommand("dsgame").setExecutor(new DsgameCommExec(dsgame, this));
        getCommand("dsgame").setTabCompleter(new DsgameCommExec(dsgame, this));

        getServer().getPluginManager().registerEvents(new EventListener(dsgame, this), this);

        getLogger().info("Deathswap plugin enabled");
    }

    @Override
    public void onDisable() {
        dsgame.endGame(true); //Stops the game
        getLogger().info("Deathswap plugin disabled");
    }

    //Loads values from config.yml and sets the appropriate values in game.
    public void loadConfig(){
        System.out.println("Loading config.yml");
        getConfig().addDefault("max-swap-time", def_swaptimemax);
        getConfig().addDefault("min-swap-time", def_swaptimemin);
        getConfig().addDefault("difficulty", def_difficulty);
        getConfig().addDefault("gameWorld-name", def_tempworld_name);
        getConfig().options().copyDefaults(true);

        saveConfig();
        reloadConfig();

        dsgame.setMaxTime(getConfig().getDouble("max-swap-time"));
        dsgame.setMinTime(getConfig().getDouble("min-swap-time"));
        dsgame.setDifficulty(getConfig().getString("difficulty"));
        dsgame.setWorldName(getConfig().getString("gameWorld-name"));

    }
}
