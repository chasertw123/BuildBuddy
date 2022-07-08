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
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class BlockInteractListener implements Listener {

    private static final List<Material> BLACKLIST = List.of(
            Material.TWISTING_VINES,
            Material.WEEPING_VINES,
            Material.SUGAR_CANE,
            Material.KELP,
            Material.CACTUS
    );

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

        if (block.getBlockData() instanceof Ageable ageable && !BLACKLIST.contains(block.getType())) {
            if (block.getType() == Material.MANGROVE_PROPAGULE && !((Hangable) block.getBlockData()).isHanging())
                return;

            int newAge = switch (block.getType()) {
                case CHORUS_FLOWER -> ageable.getAge() == 5 ? 0 : 5; // Skip stages without change
                case NETHER_WART -> ageable.getAge() == 1 ? 3 : ageable.getAge() + 1; // Skip growth stage without visuals
                default -> ageable.getAge() + 1; // Increase age by one stage
            };

            ageable.setAge(newAge > ageable.getMaximumAge() ? 0 : newAge);
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

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent event) {
        if (BuildModeManager.isActive(event.getPlayer()))
            event.setCancelled(true);
    }
}
