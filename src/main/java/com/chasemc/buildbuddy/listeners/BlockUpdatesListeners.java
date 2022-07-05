package com.chasemc.buildbuddy.listeners;

import com.chasemc.buildbuddy.Main;
import com.chasemc.buildbuddy.BuildModeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Hangable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

@SuppressWarnings("unused")
public class BlockUpdatesListeners implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer()))
            return;

        event.setCancelled(true);
        if (BuildModeManager.containsMaterial(event.getBlock().getType()))
            return;

        BlockData data = event.getBlock().getBlockData().clone();
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> event.getBlock().setBlockData(data, false));
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer()))
            return;

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR, false);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer())
                || event.getHand() == EquipmentSlot.OFF_HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || !BuildModeManager.containsMaterial(event.getMaterial()))
            return;

        Material blockMaterial = switch (event.getMaterial()) {
            case COCOA_BEANS -> Material.COCOA;
            case WHEAT_SEEDS -> Material.WHEAT;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS, MELON_SLICE -> Material.MELON_STEM;
            default -> event.getMaterial();
        };

        Block rel = Objects.requireNonNull(event.getClickedBlock()).getRelative(event.getBlockFace());
        if (rel.getType() != Material.AIR)
            return;

        rel.setType(blockMaterial, false);

        switch (blockMaterial) {
            case LADDER -> {
                Directional directional = (Directional) rel.getBlockData();
                directional.setFacing(event.getBlockFace());
                rel.setBlockData(directional, false);
            }
            case COCOA -> {
                Directional directional = (Directional) rel.getBlockData();
                directional.setFacing(event.getBlockFace().getOppositeFace());
                rel.setBlockData(directional, false);
            }

            case MANGROVE_PROPAGULE -> {
                if (event.getBlockFace() != BlockFace.DOWN)
                    break;

                Hangable hangable = (Hangable) rel.getBlockData();
                hangable.setHanging(true);
                rel.setBlockData(hangable, false);
            }
        }

        Sound sound;
        try {
            sound = Sound.valueOf(String.format("BLOCK_%s_PLACE", event.getMaterial().name()));
        } catch (IllegalArgumentException e) {
            sound = Sound.BLOCK_GRASS_PLACE;
        }

        event.getPlayer().playSound(rel.getLocation(), sound, 1f, 1f);
        event.getPlayer().swingMainHand();

        event.setCancelled(true);
    }
}
