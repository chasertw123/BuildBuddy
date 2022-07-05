package com.chasemc.buildbuddy.listeners;

import com.chasemc.buildbuddy.BlockNameCopierManager;
import com.chasemc.buildbuddy.BuildModeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BlockInteractListener implements Listener {

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClickBlock(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() == EquipmentSlot.OFF_HAND
                || event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (BlockNameCopierManager.isCopying(event.getPlayer())) {
            boolean success = BlockNameCopierManager.addBlock(event.getPlayer(), block);
            event.getPlayer().sendMessage(success
                    ? ChatColor.GREEN + "Added " + block.getType().name().toLowerCase() + " to list!"
                    : ChatColor.RED + "That block is already in the list!");
            return;
        }

        if (!(BuildModeManager.isActive(player) && !player.isSneaking()))
            return;

        if (block.getBlockData() instanceof Ageable ageable) {
            if (block.getType() == Material.MANGROVE_PROPAGULE && !((Hangable) block.getBlockData()).isHanging())
                return;

            int newAge = ageable.getAge() + 1;
            if (newAge > ageable.getMaximumAge())
                newAge = 0;

            ageable.setAge(newAge);
            block.setBlockData(ageable, false);
            player.playSound(event.getPlayer().getLocation(), Sound.ITEM_BONE_MEAL_USE, 1F, 1F);
            event.setCancelled(true);
            return;
        }

        switch (block.getType()) {
            case IRON_DOOR, IRON_TRAPDOOR -> {
                Openable data = (Openable) event.getClickedBlock().getBlockData();
                data.setOpen(!data.isOpen());

                block.setBlockData(data, false);
                event.setCancelled(true);
            }
        }
    }
}
