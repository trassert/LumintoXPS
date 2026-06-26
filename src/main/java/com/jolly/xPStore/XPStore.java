package com.jolly.xPStore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class XPStore extends JavaPlugin implements Listener {
    private static final String PERMISSION_USE = "xpstore.use";
    private static final int EXPERIENCE_COST = 30;
    private static final float SOUND_VOLUME = 1.0f;
    private static final float SOUND_PITCH = 1.25f;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("XPStore has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("XPStore has been disabled!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.ENCHANTING_TABLE)
            return;
        Player player = event.getPlayer();
        if (!player.hasPermission(PERMISSION_USE))
            return;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() != Material.GLASS_BOTTLE)
            return;
        int totalXP = getTotalExperience(player);
        int maxBottles = totalXP / EXPERIENCE_COST;
        if (maxBottles <= 0)
            return;
        int bottlesToMake = player.isSneaking() ? Math.min(maxBottles, mainHand.getAmount()) : 1;
        event.setCancelled(true);
        player.giveExp(-(bottlesToMake * EXPERIENCE_COST));
        player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, bottlesToMake));
        player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, bottlesToMake));
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SOUND_VOLUME,
                SOUND_PITCH);
    }

    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        int exp = Math.round(getExpToLevel(level) * player.getExp());

        if (level <= 15) {
            exp += level * level + 6 * level;
        } else if (level <= 30) {
            exp += (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            exp += (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        return exp;
    }

    private int getExpToLevel(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;
        if (level >= 15)
            return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }
}