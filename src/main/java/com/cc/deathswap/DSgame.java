package com.cc.deathswap;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class DSgame {

    public String gameWorldName = "dsgame-temporaryworld"; //the name of tempworld
    public static final float odist = 2000.0f;

    //Values, that can be set by players
    private double swaptimemin; //Values for the minimal and maximal time between swaps
    private double swaptimemax;
    private Difficulty difficulty;

    public DSMainPlugin jplugin;

    ArrayList<DSplayer> players; // ArrayList to hold all the players regardless of the game state
    boolean lobby; // true : we are in the lobby, false : the game has started

    public boolean swapping = false;

    WorldCreator wc;
    World gameWorld;
    Random seedRandom;

    public DSgame(DSMainPlugin plugin) {
        jplugin = plugin;
        players = new ArrayList<DSplayer>();
        lobby = true;
        wc = new WorldCreator(gameWorldName);
        seedRandom = new Random(System.currentTimeMillis());
    }

    public void resetSettings() {
        jplugin.loadConfig();
    }

    public void setMaxTime(double mt) {
        swaptimemax = mt;
        broadcastToPlayers("max-swap-time has been set to " + mt + " seconds.");
    }

    public void setMinTime(double mt) {
        swaptimemin = mt;
        broadcastToPlayers("min-swap-time has been set to " + mt + " seconds.");
    }

    public boolean setDifficulty(@Nullable String diff) { //this function will check if the difficulty entered is valid and return true if it is.
        switch (diff) {
            case "easy":
                difficulty = Difficulty.EASY;
                break;
            case "normal":
                difficulty = Difficulty.NORMAL;
                break;
            case "hard":
                difficulty = Difficulty.HARD;
                break;
            case "peaceful":
                difficulty = Difficulty.PEACEFUL;
                break;
            default:
                return false;
        }
        broadcastToPlayers("difficulty has been set to " + diff + ".");
        return true;
    }

    public boolean setWorldName(@Nullable String name) {
        if (name == null || name == "") {
            return false;
        }
        gameWorldName = name;
        wc = new WorldCreator(gameWorldName);
        return true;
    }

    public double getMaxTime() {
        return swaptimemax;
    }

    public double getMinTime() {
        return swaptimemin;
    }

    public String getDifficulty() {
        switch (difficulty) {
            case EASY:
                return "easy";
            case NORMAL:
                return "normal";
            case HARD:
                return "hard";
            case PEACEFUL:
                return "peaceful";
            default:
                return "I have no idea... Something is wrong, use '/reload'";
        }
    }

    //Returns true if the given player is the first int the players array.
    public boolean isFirst(Player p) {
        return players.size() > 0 && players.get(0).hasEqualUUID(p);
    }

    //This runs when a player executes /dsgame join
    public int addPlayer(Player p) {
        if (lobby) {
            for (DSplayer dsp : players) {
                if (dsp.hasEqualUUID(p)) {
                    return -1;
                }
            }

            broadcastToPlayers(p.getDisplayName() + " has joined the lobby.");
            String lobbyplayers = ChatColor.YELLOW + "Already in lobby: ";
            for (DSplayer dsp : players) {
                lobbyplayers += dsp.player.getDisplayName() + ", ";
            }
            if (players.size() > 0) {
                p.sendRawMessage(lobbyplayers);
            }

            players.add(new DSplayer(p.getUniqueId()));
            players.get(players.size() - 1).setPlugin(jplugin);
            return 0;
        } else {
            return -2;
        }
    }

    //This runs when a player executes /dsgame leave
    public int removePlayer(Player p) {
        if (lobby) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).hasEqualUUID(p)) {
                    players.remove(i);
                    broadcastToPlayers(ChatColor.YELLOW + p.getDisplayName() + " has left the lobby.");
                    if (players.size() >= 1 && i == 0) {
                        players.get(0).player.sendRawMessage(ChatColor.YELLOW + "Only you can start the game with '/dsgame start'");
                    }
                    return 0;
                }
            }
            return -2;
        }
        return -1;
    }

    //This runs when a player executes /dsgame start
    public int startGame(Player p) {
        if (players.size() < 2) {
            return -1;
        }
        if (!players.get(0).hasEqualUUID(p)) {
            return -2;
        }
        if (!lobby) {
            return -3;
        }

        lobby = false;

        for (DSplayer dsp : players) {
            if (!dsp.setPlayer()) {
                players.remove(dsp);
            }
        }

        if (players.size() < 2) {
            return -1;
        }

        broadcastToPlayers("Generating minigame world. The DeathSwap game will start soon!");
        Bukkit.getServer().unloadWorld(gameWorld, false);

        deleteTempWorld();

        wc.copy(Bukkit.getServer().getWorlds().get(0));
        wc.seed(seedRandom.nextLong());
        gameWorld = wc.createWorld();
        gameWorld.setKeepSpawnInMemory(false);
        gameWorld.setDifficulty(difficulty);

        scatterPlayers();

        return 0;
    }

    //At the start of the game this will teleport every player to their random location
    //1. set some constant math values
    //2. seek the top block of the given 2d coordinates
    //3. start the swapper loop and teleport players
    public void scatterPlayers() {
        doMaths(players.size());

        for (int i = 0; i < players.size(); i++) {
            Vector2d v = getStartingCoordinates(i);
            gameWorld.loadChunk((int) Math.floor(v.x / 16), (int) Math.floor(v.y / 16), true);
            int y = 255;
            while (gameWorld.getBlockAt((int) Math.floor(v.x), y--, (int) Math.floor(v.y)).getType() == Material.AIR) ;
            y++;

            players.get(i).start(new Location(gameWorld, Math.floor(v.x), y, Math.floor(v.y)));

            gameWorld.unloadChunk((int) Math.floor(v.x / 16), (int) Math.floor(v.y / 16), true);
        }

        broadcastToPlayers("DeathSwap Game started. Good Luck");
        startSwapping();
        checkGameState();
    }

    //This runs when a player dies in a deathswap game
    public boolean playerDeath(Player p) {
        for (DSplayer dsp : players) {
            if (dsp.hasEqualUUID(p)) {
                dsp.die();
                broadcastToPlayers(dsp.player.getDisplayName() + " has died.");
                checkGameState();
                return true;
            }
        }
        return false;
    }

    //returns true if the player is playing and is still alive
    public boolean isPlaying(Player p) {
        for (DSplayer dsp : players) {
            if (dsp.hasEqualUUID(p) || dsp.state == DSplayer.DsPlayerState.Alive) {
                return true;
            }
        }
        return false;
    }

    //This runs when a player executes /dsgame stop
    public int playerForfeit(Player p) {
        if (!lobby) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).hasEqualUUID(p) && players.get(i).state != DSplayer.DsPlayerState.Left) {
                    players.get(i).leave();
                    checkGameState();
                    return 0;
                }
            }
            return -2;
        }
        return -1;
    }

    //checks whether there is only one player alive, if so, it ends the game
    public void checkGameState() {
        int aliveCount = 0;
        for (DSplayer dsp : players) {
            if (dsp.state == DSplayer.DsPlayerState.Alive) {
                aliveCount++;
            }
        }
        if (aliveCount <= 1) {
            DSplayer winner = null;
            for (DSplayer dsp : players) {
                if (dsp.state == DSplayer.DsPlayerState.Alive) {
                    winner = dsp;
                }
            }
            if (winner == null) {
                broadcastToPlayers("A problem has occurred, aborting game.");
                endGame(false);
                return;
            }
            broadcastToPlayers(winner.player.getDisplayName() + " won the DeathSwap game. Score: " + winner.score);
            endGame(false);
            broadcastToPlayers("Scores are:");
            for (DSplayer dsp : players) {
                broadcastToPlayers(dsp.player.getDisplayName() + " : " + dsp.score);
            }

        }
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void broadcastToPlayers(ChatColor color, String message) {
        for (DSplayer dsp : players) {
            if (dsp.player == null && !dsp.setPlayer()) {
                continue;
            }
            dsp.player.sendRawMessage(color + message);
        }
    }

    public void broadcastToPlayers(String message) {
        broadcastToPlayers(ChatColor.YELLOW, message);
    }

    // This ends the game
    // fast = true; -> will use DSplayer.leave() instead of DSplayer.leaveNextTick(). Shouldn't generally be used
    // fast = false -> will use DSplayer.leaveNextTick()
    public void endGame(boolean fast) {
        stopSwapping();

        if (!lobby) {
            lobby = true;
            for (DSplayer dsp : players) {
                if (fast) {
                    dsp.leave();
                } else {
                    dsp.leaveNextTick();
                }
            }
            players = new ArrayList<DSplayer>();
            Bukkit.getServer().unloadWorld(gameWorld, false);
        } else {
            broadcastToPlayers("Everyone was kicked out of the lobby");
            players = new ArrayList<DSplayer>();
        }
    }

    //This starts the swapper - the class that handles the swapping
    protected void startSwapping() {
        swapping = true;
        long time = getRandSwapTime();
        Bukkit.getServer().getScheduler().runTaskLater(jplugin, new CountDownTimer(this), time - 10 * 20);
        Bukkit.getServer().getScheduler().runTaskLater(jplugin, new Swapper(this), time);
    }

    protected void stopSwapping() {
        swapping = false;
    }

    //Returns a random value between swaptimemax and swaptimemin
    public long getRandSwapTime() {
        return (long) getMaxTime() * 20;
    }

    //This sets some values with which the random position of the players will be calculated
    private Vector2d sco;
    private double alpha, range;

    private void doMaths(int numOfPlayers) {

        sco = new Vector2d((float) Math.random() * odist, (float) Math.random() * odist);
        range = numOfPlayers * odist;

        if (numOfPlayers <= 1) {
            alpha = Math.PI / 2;
        } else {
            alpha = 2 * Math.PI / numOfPlayers;
        }

    }

    //Returns the starting position of a player for the given index
    private Vector2d getStartingCoordinates(int index) {
        return new Vector2d(Math.cos(index * alpha) * range + sco.x, Math.sin(index * alpha) * range + sco.y);
    }

    //this is the function that is triggered by the swapper
    //This swaps the players around
    public void swap() {
        Location prevLoc;
        int i = 0;
        while (players.get(i).state != DSplayer.DsPlayerState.Alive) {
            i++;
        }
        prevLoc = players.get(i).player.getLocation();
        for (; i < players.size() - 1; i++) {
            int f = i + 1;
            while (players.get(f).state != DSplayer.DsPlayerState.Alive && f < players.size()) {
                f++;
            }
            if (f >= players.size()) {
                break;
            }
            players.get(i).teleport(players.get(f).player.getLocation());
            players.get(i).score++;
            i = f - 1;
        }
        players.get(i).teleport(prevLoc);
    }

    //A function to delete the temporary gameworld
    public void deleteTempWorld() {
        File wcont = Bukkit.getServer().getWorldContainer();
        for (File f : wcont.listFiles()) {
            if (f.getName().equals(gameWorldName)) {
                deleteFolder(f);
            }
        }
    }

    //This function deletes a folder recursively
    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                deleteFolder(f);
            }
        }
        folder.delete();
    }

    public static class CountDownTimer implements Runnable {

        public DSgame game;

        public CountDownTimer(DSgame game) {
            this.game = game;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                final int finali = i;
                Bukkit.getServer().getScheduler().runTaskLater(game.jplugin, () -> game.broadcastToPlayers(ChatColor.RED, "Swapping In " + (10 - finali) + " Seconds"), i * 20);
            }
        }
    }
}

