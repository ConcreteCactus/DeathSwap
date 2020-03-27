package com.cc.deathswap;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class DSplayer {
    public Player player;
    int score;
    UUID uuid;
    GameMode prevGameMode;

    ArmorStand as; // an armor stand to hold some inforamtion about the player currently playing deathswap game

    JavaPlugin jp;

    public Location prevLocation;
    double prevHealth = 20.0;
    int prevHunger = 20;
    Collection<PotionEffect> prevEffects;

    enum DsPlayerState{
        Alive, Dead, Left
    };

    DsPlayerState state;

    public DSplayer(UUID id){
        uuid = id;
        score = 0;
    }

    //A function to set the Player member, to be able to handle player-related tasks
    //This is here because of disconnecting and reconnecting
    public boolean setPlayer(){
        player = Bukkit.getServer().getPlayer(uuid);
        if(!player.isOnline()){
            return false;
        }
        return true;
    }

    //This saves the player state in the main world and teleports him to the game world
    public void start(Location loc){
        prevLocation = player.getLocation();
        prevGameMode = player.getGameMode();

        prevHealth = player.getHealth();
        prevHunger = player.getFoodLevel();
        prevEffects = player.getActivePotionEffects();

        for(PotionEffect pe : prevEffects){
            player.removePotionEffect(pe.getType());
        }

        as = (ArmorStand) prevLocation.getWorld().spawnEntity(prevLocation, EntityType.ARMOR_STAND); //An armor stand will be set up in the main world, because that is how I save ones inventory.
        constructArmorStand();

        score = 0;
        state = DsPlayerState.Alive;
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(loc);
        player.getInventory().clear();

        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    //This runs when the payer dies in a deathswap game.
    public void die(){
        state = DsPlayerState.Dead;
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20.0);
    }

    //This does almost the same thing as leave(), but this is less error prone
    public void leaveNextTick(){
        if(jp == null){
            leave();
            return;
        }
        Bukkit.getServer().getScheduler().runTask(jp, new PlayerLeaveRunner(this));
    }

    //This runs when a player leaves the game
    //This teleports the player back to the main world and sets the pre-game state
    public void leave(){
        state = DsPlayerState.Left;
        player.setGameMode(prevGameMode);
        player.teleport(prevLocation);

        Inventory mainSI = ((ShulkerBox) ((BlockStateMeta) as.getEquipment().getItemInMainHand().getItemMeta()).getBlockState()).getInventory();
        Inventory offSI = ((ShulkerBox) ((BlockStateMeta) as.getEquipment().getItemInOffHand().getItemMeta()).getBlockState()).getInventory();

        for(int i = 0; i < mainSI.getSize(); i++){
            player.getInventory().setItem(i, mainSI.getItem(i));
        }

        for(int i = mainSI.getSize(); i < player.getInventory().getSize(); i++){
            player.getInventory().setItem(i, offSI.getItem(i-mainSI.getSize()));
        }

        as.remove();

        player.setHealth(prevHealth);
        player.setFoodLevel(prevHunger);

        Collection<PotionEffect> peffects = player.getActivePotionEffects();
        for(PotionEffect pe : peffects){
            player.removePotionEffect(pe.getType());
        }
        for(PotionEffect pe : prevEffects){
            player.addPotionEffect(pe);
        }

    }

    //A small function to check if the player is connected to the same IRL person
    public boolean hasEqualUUID(Player p){
        return uuid.equals(p.getUniqueId());
    }

    //This sets up the armor stand that will hold the players inventory
    //It will only be destructible by the player that has the same UUID by a right-click, it will also call the leave() function
    private void constructArmorStand(){
        as.setArms(true);
        as.setBasePlate(false);
        as.setGravity(false);
        as.setInvulnerable(true);
        as.setCustomName(player.getDisplayName());
        as.setCustomNameVisible(true);
        if(jp != null){
            as.getPersistentDataContainer().set(new NamespacedKey(jp, "forDSGAME"), PersistentDataType.PrimitivePersistentDataType.STRING, player.getUniqueId().toString());
        }

        as.getEquipment().setHelmet(player.getEquipment().getHelmet());
        as.getEquipment().setChestplate(player.getEquipment().getChestplate());
        as.getEquipment().setLeggings(player.getEquipment().getLeggings());
        as.getEquipment().setBoots(player.getEquipment().getBoots());

        ItemStack mainHand = new ItemStack(Material.BLACK_SHULKER_BOX);
        ItemStack offHand = new ItemStack(Material.BLACK_SHULKER_BOX);

        ItemStack[] pinv = player.getInventory().getContents();

        ItemMeta mainItemMeta = mainHand.getItemMeta();
        BlockState mainBlockState = ((BlockStateMeta) mainItemMeta).getBlockState();
        Inventory mainShulkerInventory = ((ShulkerBox) mainBlockState).getInventory();
        for(int i = 0; i < mainShulkerInventory.getSize(); i++){
            mainShulkerInventory.setItem(i, pinv[i]);
        }
        ((BlockStateMeta) mainItemMeta).setBlockState(mainBlockState);
        mainHand.setItemMeta(mainItemMeta);

        ItemMeta offItemMeta = mainHand.getItemMeta();
        BlockState offBlockState = ((BlockStateMeta) mainItemMeta).getBlockState();
        Inventory offShulkerInventory = ((ShulkerBox) offBlockState).getInventory();
        for(int i = mainShulkerInventory.getSize(); i < pinv.length; i++){
            offShulkerInventory.setItem(i-mainShulkerInventory.getSize(), pinv[i]);
        }
        ((BlockStateMeta) offItemMeta).setBlockState(offBlockState);
        offHand.setItemMeta(offItemMeta);

        as.getEquipment().setItemInMainHand(mainHand);
        as.getEquipment().setItemInOffHand(offHand);


    }

    public void teleport(Location loc){
        player.teleport(loc);
    }

    public void setPrevLocation(Location prevLoc){
        prevLocation = prevLoc;
    }

    public void setPrevGameMode(GameMode gm){
        prevGameMode = gm;
    }

    public void setArmorStand(ArmorStand armorStand){
        as = armorStand;
    }

    public void setPlugin(JavaPlugin plugin){
        jp = plugin;
    }

}
