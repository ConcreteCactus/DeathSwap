package com.rh.deathswap;

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
    ArmorStand as;

    JavaPlugin jp;

    double prevHealth = 20.0;
    int prevHunger = 20;
    Collection<PotionEffect> prevEffects;

    enum DsPlayerState{
        Alive, Dead, Left
    };

    DsPlayerState state;

    public Location prevLocation;

    public DSplayer(UUID id){
        uuid = id;
        score = 0;
    }

    public boolean setPlayer(){
        player = Bukkit.getServer().getPlayer(uuid);
        if(!player.isOnline()){
            return false;
        }
        return true;
    }

    public void start(Location loc){
        prevLocation = player.getLocation();
        prevGameMode = player.getGameMode();

        prevHealth = player.getHealth();
        prevHunger = player.getFoodLevel();
        prevEffects = player.getActivePotionEffects();

        for(PotionEffect pe : prevEffects){
            player.removePotionEffect(pe.getType());
        }

        as = (ArmorStand) prevLocation.getWorld().spawnEntity(prevLocation, EntityType.ARMOR_STAND);
        constructArmorStand();

        score = 0;
        state = DsPlayerState.Alive;
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(loc);
        player.getInventory().clear();

        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    public void die(){
        state = DsPlayerState.Dead;
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20.0);
    }

    public void leaveNextTick(){
        if(jp == null){
            leave();
            return;
        }
        Bukkit.getServer().getScheduler().runTask(jp, new PlayerLeaveRunner(this));
    }

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

    public boolean hasEqualUUID(Player p){
        return uuid.equals(p.getUniqueId());
    }

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
