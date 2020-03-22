package com.rh.deathswap;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class DSgame {
    ArrayList<DSplayer> players;
    boolean lobby;

    public boolean trunning = false;

    WorldCreator wc;
    World gameWorld;

    Random seedRandom;

    public JavaPlugin jplugin;

    public static final String gameWorldName = "dsgame-temporaryworld";
    public static final float odist = 2000.0f;

    public static final double swaptimemin = 100.0;
    public static final double swaptimemax = 150.0;


    public DSgame(JavaPlugin plugin){
        jplugin = plugin;
        players = new ArrayList<DSplayer>();
        lobby = true;
        wc = new WorldCreator(gameWorldName);
        seedRandom = new Random(System.currentTimeMillis());
    }

    public boolean addPlayer(Player p){
        if(lobby){
            for(DSplayer dsp : players){
                if(dsp.hasEqualUUID(p)){return false;}
            }
            broadcastToPlayers(p.getDisplayName() + " has joined the lobby.");
            String lobbyplayers = "Already in lobby: ";
            for(DSplayer dsp : players){
                dsp.setPlayer();
                lobbyplayers += dsp.player.getDisplayName() + ", ";
            }
            if(players.size() > 0){
                p.sendRawMessage(lobbyplayers);
            }

            players.add(new DSplayer(p.getUniqueId()));
            players.get(players.size()-1).setPlugin(jplugin);
            return true;
        }else{
            return false;
        }
    }

    public boolean removePlayer(Player p){
        if(lobby) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).hasEqualUUID(p)) {
                    players.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean startGame(Player p){
        if(players.size() < 2 || !players.get(0).hasEqualUUID(p) || !lobby){
            return false;
        }
        lobby = false;

        for(DSplayer dsp : players){
            if(!dsp.setPlayer()){
                players.remove(dsp);
            }
        }

        if(players.size() < 2){
            return false;
        }

        broadcastToPlayers("Generating minigame world. The DeathSwap game will start soon!");
        Bukkit.getServer().unloadWorld(gameWorld, false);

        deleteTempWorld();

        wc.copy(players.get(0).player.getWorld());
        wc.seed(seedRandom.nextLong());
        gameWorld = wc.createWorld();
        gameWorld.setKeepSpawnInMemory(false);

        scatterPlayers();

        return true;
    }

    public void scatterPlayers(){
        doMaths(players.size());

        for(int i = 0; i < players.size(); i++){
            Vector2d v = getStartingCoordinates(i);
            gameWorld.loadChunk((int)Math.floor(v.x/16), (int)Math.floor(v.y/16), true);
            int y = 255;
            while(gameWorld.getBlockAt((int)Math.floor(v.x), y--, (int)Math.floor(v.y)).getType() == Material.AIR);
            y++;

            players.get(i).start(new Location(gameWorld, Math.floor(v.x), y, Math.floor(v.y)));

            gameWorld.unloadChunk((int)Math.floor(v.x/16), (int)Math.floor(v.y/16), true);
        }

        broadcastToPlayers("DeathSwap Game started. Good Luck");
        startThread();
        checkGameState();
    }

    public boolean playerDeath(Player p){
        for(DSplayer dsp : players){
            if(dsp.hasEqualUUID(p)){
                dsp.die();
                checkGameState();
                return true;
            }
        }
        return false;
    }

    public boolean isPlaying(Player p){
        for(DSplayer dsp : players){
            if(dsp.hasEqualUUID(p)){
                return true;
            }
        }
        return false;
    }

    public boolean playerForfeit(Player p){
        if(!lobby){
           for(int i = 0; i < players.size(); i++){
               if(players.get(i).hasEqualUUID(p) && players.get(i).state != DSplayer.DsPlayerState.Left){
                   players.get(i).leave();
                   checkGameState();
                   return true;
               }
           }
        }
        return false;
    }

    public void checkGameState(){
        int aliveCount = 0;
        for(DSplayer dsp : players){
            if(dsp.state == DSplayer.DsPlayerState.Alive){aliveCount++;}
        }
        if(aliveCount <= 1){
            DSplayer winner = null;
            for(DSplayer dsp : players){
                if(dsp.state == DSplayer.DsPlayerState.Alive){
                    winner = dsp;
                }
            }
            if(winner == null){
                broadcastToPlayers("A problem has occurred, aborting game.");
                endGame();
                return;
            }
            broadcastToPlayers(winner.player.getDisplayName() + " won the DeathSwap game. Score: " + winner.score);
            endGame();
            broadcastToPlayers("Scores are:");
            for(DSplayer dsp : players){
                broadcastToPlayers(dsp.player.getDisplayName() + " : " + dsp.score);
            }

        }
    }

    public int getPlayerCount(){
        return players.size();
    }

    public void broadcastToPlayers(String message){
        for(DSplayer dsp : players){
            dsp.player.sendRawMessage(message);
        }
    }

    public void endGame(){
        stopThread();

        lobby = true;
        for(DSplayer dsp : players){
            dsp.leaveNextTick();
        }
        players = new ArrayList<DSplayer>();
        Bukkit.getServer().unloadWorld(gameWorld, false);
    }

    protected void startThread(){
        trunning = true;
        Bukkit.getServer().getScheduler().runTaskLater(jplugin, new Swapper(this), (long)getRandSwapTime() * 20);
    }

    protected void stopThread(){
        trunning = false;
    }

    public double getRandSwapTime(){
        double time = (Math.random() * (swaptimemax - swaptimemin)) + swaptimemin;
        return time;
    }

    private Vector2d sco;
    private double alpha, range;
    private void doMaths(int numOfPlayers){

        sco = new Vector2d((float)Math.random() * odist, (float)Math.random() * odist);
        range = numOfPlayers * odist;

        if(numOfPlayers <= 1){
            alpha = Math.PI / 2;
        }else{
            alpha = 2 * Math.PI / numOfPlayers;
        }

    }

    private Vector2d getStartingCoordinates(int index){
        return new Vector2d(Math.cos(index * alpha) * range + sco.x, Math.sin(index * alpha) * range + sco.y);
    }

    public void swap() {
        Location prevLoc;
        int i = 0;
        while(players.get(i).state != DSplayer.DsPlayerState.Alive){
            i++;
        }
        prevLoc = players.get(i).player.getLocation();
        for(; i < players.size()-1; i++){
            int f = i+1;
            while(players.get(f).state != DSplayer.DsPlayerState.Alive && f < players.size()){
                f++;
            }
            if(f >= players.size()){
                break;
            }
            players.get(i).teleport(players.get(f).player.getLocation());
            players.get(i).score++;
            i = f-1;
        }
        players.get(i).teleport(prevLoc);
    }

    public void deleteTempWorld(){
        File wcont = Bukkit.getServer().getWorldContainer();
        for(File f : wcont.listFiles()){
            if(f.getName().equals(gameWorldName)){
                deleteFolder(f);
            }
        }
    }

    private void deleteFolder(File folder){
        if(folder.isDirectory()){
            for(File f : folder.listFiles()){
                deleteFolder(f);
            }
        }
        folder.delete();
    }
}

