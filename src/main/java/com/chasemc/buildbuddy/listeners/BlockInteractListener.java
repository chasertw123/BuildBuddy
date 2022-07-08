package com.chasemc.buildbuddy.listeners;

import com.chasemc.buildbuddy.BlockNameCopierManager;
import com.chasemc.buildbuddy.BuildModeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.List;

public class BlockInteractListener implements Listener {

    private static final List<Material> BLACKLIST = List.of(
            Material.TWISTING_VINES,
            Material.WEEPING_VINES,
            Material.SUGAR_CANE,
            Material.KELP,
            Material.CACTUS
    );

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (this.isInvalidInteraction(event) || !BlockNameCopierManager.isCopying(event.getPlayer()))
            return;

        assert event.getClickedBlock() != null;
        boolean success = BlockNameCopierManager.addBlock(event.getPlayer(), event.getClickedBlock());
        event.getPlayer().sendMessage(success
                ? ChatColor.GREEN + "Added " + event.getClickedBlock().getType().name().toLowerCase() + " to list!"
                : ChatColor.RED + "That block is already in the list!");

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickAgeableBlock(PlayerInteractEvent event) {
        if (this.isInvalidInteraction(event) || !BuildModeManager.isActive(event.getPlayer())
                || (event.getPlayer().isSneaking() && event.getItem() != null))
            return;

        assert event.getClickedBlock() != null;
        event.setCancelled(this.handleClickingAgeableBlock(event.getClickedBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickOpenableBlock(PlayerInteractEvent event) {
        if (this.isInvalidInteraction(event) || !BuildModeManager.isActive(event.getPlayer())
                || (event.getPlayer().isSneaking() && event.getItem() != null))
            return;

        assert event.getClickedBlock() != null;
        event.setCancelled(this.handleClickingOpenableBlock(event.getClickedBlock()));
    }

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent event) {
        if (BuildModeManager.isActive(event.getPlayer()))
            event.setCancelled(true); // Prevent the harvest of blocks while trying to change their age.
    }

    private boolean isInvalidInteraction(PlayerInteractEvent event) {
        return event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || event.getClickedBlock() == null;
    }

    private boolean handleClickingAgeableBlock(Block block) {
        if (block.getBlockData() instanceof Ageable ageable && !BLACKLIST.contains(block.getType())) {
            if (block.getType() == Material.MANGROVE_PROPAGULE && !((Hangable) block.getBlockData()).isHanging())
                return false;

            int newAge = switch (block.getType()) {
                case CHORUS_FLOWER -> ageable.getAge() == 5 ? 0 : 5; // Skip stages without change
                case NETHER_WART -> ageable.getAge() == 1 ? 3 : ageable.getAge() + 1; // Skip growth stage without visuals
                default -> ageable.getAge() + 1; // Increase age by one stage
            };

            ageable.setAge(newAge > ageable.getMaximumAge() ? 0 : newAge);
            block.setBlockData(ageable, false);
            block.getWorld().playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1F, 1F);
            return true;
        }

        return false;
    }

    private boolean handleClickingOpenableBlock(Block block) {
        if (block.getBlockData() instanceof Door door) {
            door.setOpen(!door.isOpen());
            block.setBlockData(door, false);

            Vector offset = new Vector(0, door.getHalf() == Bisected.Half.BOTTOM ? 1 : -1, 0);
            Block otherHalf = block.getLocation().clone().add(offset).getBlock();
            if (otherHalf.getType() == block.getType()) {
                door = (Door) otherHalf.getBlockData();
                door.setOpen(!door.isOpen());
                otherHalf.setBlockData(door, false);
            }

            if (block.getType() == Material.IRON_DOOR)
                block.getWorld().playSound(block.getLocation(), door.isOpen() ? Sound.BLOCK_IRON_DOOR_OPEN
                        : Sound.BLOCK_IRON_DOOR_CLOSE, 1F, 1F);

            return true;
        }

        else if (block.getBlockData() instanceof TrapDoor trapDoor) {
            trapDoor.setOpen(!trapDoor.isOpen());
            block.setBlockData(trapDoor, false);
            if (block.getType() == Material.IRON_TRAPDOOR)
                block.getWorld().playSound(block.getLocation(), trapDoor.isOpen() ? Sound.BLOCK_IRON_TRAPDOOR_OPEN
                        : Sound.BLOCK_IRON_TRAPDOOR_CLOSE , 1F, 1F);

            return true;
        }

        else if (block.getBlockData() instanceof Gate gate) {
            gate.setOpen(!gate.isOpen());
            block.setBlockData(gate, false);
            return true;
        }

        return false;
    }
}
