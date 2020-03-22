package com.rh.deathswap;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class EventListener implements Listener {

    DSgame game;
    JavaPlugin plugin;

    public EventListener(DSgame dsgame, JavaPlugin javaPlugin){
        game = dsgame;
        plugin = javaPlugin;
    }

    @EventHandler
    public void onWorldInitEvent(WorldInitEvent event){
        if(event.getWorld().getName() == game.gameWorldName){
            event.getWorld().setKeepSpawnInMemory(false);
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        String tag = event.getRightClicked().getPersistentDataContainer().get(new NamespacedKey(plugin, "forDSGAME"), PersistentDataType.STRING);
        if(tag != null){
            if(tag.equals(event.getPlayer().getUniqueId().toString())){
                DSplayer temp = new DSplayer(event.getPlayer().getUniqueId());
                temp.setPlayer();
                temp.setPrevLocation(event.getRightClicked().getLocation());
                temp.setPrevGameMode(event.getPlayer().getGameMode());
                temp.setArmorStand(event.getRightClicked());
                temp.leave();

                event.setCancelled(true);
            }else{
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(EntityDamageEvent event){
        if((event.getEntity() instanceof Player) && game.isPlaying((Player) event.getEntity()) && event.getFinalDamage() >= ((Player) event.getEntity()).getHealth()){
            game.playerDeath((Player) event.getEntity());
            event.setCancelled(true);
        }
    }
}
