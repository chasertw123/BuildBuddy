package com.chasemc.buildbuddy.listeners;

import com.chasemc.buildbuddy.BuildModeManager;
import com.chasemc.buildbuddy.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

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
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            if (data.getMaterial() == Material.POWDER_SNOW && event.getBlock().getType() != Material.AIR)
                return;

            event.getBlock().setBlockData(data, false);
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer()))
            return;

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR, false);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPlaceBlockWithBucket(PlayerBucketEmptyEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer()))
            return;

        event.setCancelled(true);
        switch (event.getBucket()) {
            case LAVA_BUCKET -> {
                if (event.getBlock().getType() == Material.AIR)
                    event.getBlock().setType(Material.LAVA, false);
            }
            // NOTE: This event is not called when placing a powdered snow bucket
            case WATER_BUCKET -> {
                if (event.getBlockClicked().getBlockData() instanceof Waterlogged waterlogged && !waterlogged.isWaterlogged()) {
                    waterlogged.setWaterlogged(true);
                    event.getBlockClicked().setBlockData(waterlogged, false);
                }

                else if (event.getBlock().getType() == Material.AIR) {
                    event.getBlock().setType(Material.WATER, false);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEmptyBlockWithBucket(PlayerBucketFillEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer()))
            return;

        event.setCancelled(true);
        switch (event.getBlock().getType()) {
            case WATER, LAVA, POWDER_SNOW -> {
                event.getBlock().setType(Material.AIR, false);
                return;
            }
        }

        if (event.getBlock().getBlockData() instanceof Waterlogged waterlogged) {
            waterlogged.setWaterlogged(false);
            event.getBlock().setBlockData(waterlogged, false);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!BuildModeManager.isActive(event.getPlayer())
                || event.getHand() == EquipmentSlot.OFF_HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || !BuildModeManager.containsMaterial(event.getMaterial()))
            return;

        Material blockMaterial = BuildModeManager.materialToBlockMaterial(event.getMaterial());
        Block rel = Objects.requireNonNull(event.getClickedBlock()).getRelative(event.getBlockFace());
        if (rel.getType() != Material.AIR || !blockMaterial.isBlock())
            return;

        rel.setType(blockMaterial, false);

        switch (blockMaterial) {
            case LADDER -> {
                Directional directional = (Directional) rel.getBlockData();
                directional.setFacing(event.getBlockFace());
                rel.setBlockData(directional, false);
            }

            case COCOA -> {
                BlockFace direction = event.getBlockFace().getOppositeFace();
                if (event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN)
                    direction = this.lookDirectionToBlockFace(event.getPlayer().getLocation().getDirection());

                Directional directional = (Directional) rel.getBlockData();
                directional.setFacing(direction);
                rel.setBlockData(directional, false);
            }

            case TRIPWIRE_HOOK -> {
                BlockFace direction = event.getBlockFace();
                if (event.getBlockFace() == BlockFace.UP || event.getBlockFace() == BlockFace.DOWN)
                    direction = this.lookDirectionToBlockFace(event.getPlayer().getLocation().getDirection()).getOppositeFace();

                Directional directional = (Directional) rel.getBlockData();
                directional.setFacing(direction);
                rel.setBlockData(directional, false);
            }

            case MANGROVE_PROPAGULE -> {
                if (event.getBlockFace() != BlockFace.DOWN)
                    break;

                Hangable hangable = (Hangable) rel.getBlockData();
                hangable.setHanging(true);
                rel.setBlockData(hangable, false);
            }

            case LILAC, ROSE_BUSH, SUNFLOWER, PEONY, TALL_GRASS, LARGE_FERN -> {
                Bisected bisected = (Bisected) rel.getBlockData();
                bisected.setHalf(Bisected.Half.TOP);
                rel.setBlockData(bisected, false);

                Block otherHalf = rel.getRelative(BlockFace.DOWN);
                if (otherHalf.getType() != rel.getType())
                    break;

                bisected = (Bisected) otherHalf.getBlockData();
                bisected.setHalf(Bisected.Half.BOTTOM);
                otherHalf.setBlockData(bisected, false);
            }

            case WEEPING_VINES -> {
                Block block = rel.getRelative(BlockFace.UP);
                if (block.getType() == rel.getType())
                    block.setType(Material.WEEPING_VINES_PLANT, false);
            }

            case TWISTING_VINES -> {
                Block block = rel.getRelative(BlockFace.DOWN);
                if (block.getType() == rel.getType())
                    block.setType(Material.TWISTING_VINES_PLANT, false);
            }
        }

        rel.getWorld().playSound(rel.getLocation(), rel.getBlockData().getSoundGroup().getPlaceSound(), 1f, 1f);
        event.getPlayer().swingMainHand();

        event.setCancelled(true);
    }

    private BlockFace lookDirectionToBlockFace(Vector v) {
        double x = Math.abs(v.getX());
        double y = Math.abs(v.getY());
        double z = Math.abs(v.getZ());

        if (x > z)
            return v.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;

        return v.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }
}
