package com.cc.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

// This is the CommandExecutor and TabCompleter class of the /dsgame command
public class DsgameCommExec  implements CommandExecutor, TabCompleter {

    public static final String infoPath = "resources/game-info.txt";

    DSgame dsgame;
    HashMap<String, String> infoMessages;
    JavaPlugin jplugin;

    public DsgameCommExec(DSgame game, JavaPlugin plugin){
        dsgame = game;
        jplugin = plugin;

        //Load game-info.txt into infoMessages
        infoMessages = new HashMap<String, String>();

        InputStream infoStream = jplugin.getResource(infoPath);
        String gameInfo = "";
        if(infoStream != null){
            Scanner s = new Scanner(infoStream).useDelimiter("\\A");
            gameInfo = s.hasNext() ? s.next() : "";
        }

        //Parse game-info.txt
        int i = 0;
        String key, value;
        while(i < gameInfo.length()){
            key = "";
            value = "";

            while(i < gameInfo.length() && gameInfo.charAt(i) != ':'){
                if(gameInfo.charAt(i) == '\\'){i++;}
                if(gameInfo.charAt(i) == '#'){
                    while(i < gameInfo.length() && gameInfo.charAt(i) != '\n'){
                        i++;
                    }
                }
                i++;
            }
            if(i+1 >= gameInfo.length()){break;}

            i++;
            while(i < gameInfo.length() && gameInfo.charAt(i) != ':'){
                if(gameInfo.charAt(i) == '\\' &&  i < gameInfo.length()-1){
                    i++;
                }
                key += gameInfo.charAt(i);
                i++;
            }
            if(i+1 >= gameInfo.length()){break;}

            i++;
            while(i+1 < gameInfo.length() && !(gameInfo.charAt(i) == ':' && gameInfo.charAt(i+1) == ':')){
                if(gameInfo.charAt(i) == '\\' &&  i < gameInfo.length()-1){
                    i++;
                }
                value += gameInfo.charAt(i);
                i++;
            }
            i += 2;
            infoMessages.put(key, value);
        }

    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length < 1){
            return false;
        }

        switch(args[0]){
            case "join":
                onJoin(commandSender);
                break;
            case "leave":
                onLeave(commandSender);
                break;
            case "start":
                onStart(commandSender);
                break;
            case "stop":
                onStop(commandSender);
                break;
            case "info":
                onInfo(commandSender, args);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        switch(args.length){
            case 1:
                return Arrays.asList("join", "leave", "start", "stop", "info", "settings");

            case 2:
                switch(args[0]){
                    case "info":
                        return Arrays.asList(infoMessages.keySet().toArray(new String[0]));
                    case "settings":
                        return Arrays.asList("...");
                    default:
                        return null;
                }

            default:
                return null;
        }
    }

    private void onJoin(CommandSender commandSender){

        boolean first = false;
        if(dsgame.getPlayerCount() < 1){
            first = true;
        }
        int ret = dsgame.addPlayer((Player) commandSender);
        switch(ret){
            case 0:
                sendMessage(commandSender, "Joined deathswap game.");
                break;
            case -1: // Already in lobby
                sendMessage(commandSender, "You are already in the lobby.");
                break;
            case -2: // Game has already started
                sendMessage(commandSender, "The game has started, sry...");
        }

        if(first){sendMessage(commandSender, "Only you can start the game with '/dsgame start'"); }

    }

    private void onLeave(CommandSender commandSender){
        int ret = dsgame.removePlayer((Player) commandSender);
        switch(ret){
            case 0:
                sendMessage(commandSender, "You have left the lobby.");
                break;
            case -1: // Game has started
                sendMessage(commandSender, "You are not in the lobby anymore, if you want to forfeit the game, run '/dsgame stop'");
                break;
            case -2: // Player is not in lobby
                sendMessage(commandSender, "You are not in the lobby");
                break;
        }
    }

    private void onStart(CommandSender commandSender){
        int ret = dsgame.startGame((Player) commandSender);
        switch(ret){
            case 0:
                break;
            case -1: // The number of online players in lobby is less than 2
                sendMessage(commandSender, "There is not enough players in the lobby.");
                break;
            case -2: // commandSender is not the first player in the lobby
                sendMessage(commandSender, "You can't start the game.");
                break;
            case -3: // The game has already started
                sendMessage(commandSender, "The game is already running.");
        }
    }

    private void onStop(CommandSender commandSender){

        int ret = dsgame.playerForfeit((Player) commandSender);
        switch(ret){
            case 0:
                sendMessage(commandSender, "You stopped playing and forfeited the game.");
                break;
            case -1: // Player is still in lobby
                sendMessage(commandSender, "You can't stop playing, you are in the lobby. If you want to leave the lobby, run /dsgame leave");
                break;
            case -2: // Player is not in lobby
                sendMessage(commandSender, "You have already stopped playing.");
                break;
        }

    }


    private void onInfo(CommandSender commandSender, String[] args) {
        if(commandSender instanceof Player){
            if(args.length == 2 && infoMessages.containsKey(args[1])){
                sendMessage(commandSender, ChatColor.DARK_GREEN, infoMessages.get(args[1]));
            }else{
                sendMessage(commandSender, ChatColor.DARK_GREEN, infoMessages.get(""));
            }
        }
    }

    private void sendMessage(CommandSender commandSender, String message) {
        if(commandSender instanceof Player){
            ((Player) commandSender).sendRawMessage(ChatColor.YELLOW + message);
        }
    }

    private void sendMessage(CommandSender commandSender, ChatColor color, String message){
        if(commandSender instanceof Player){
            ((Player) commandSender).sendRawMessage(color + message);
        }
    }
}
