package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import com.chasemc.buildbuddy.utils.BlockUpdateOperation;
import com.chasemc.buildbuddy.utils.DistributedTask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockBiomeCommand extends PlayerCommand {


    public BlockBiomeCommand() {
        super("blockbiome", "buildbuddy.blockbiome");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Invalid Usage! /blockbiome <block> <biome> <radius>");
            return;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            player.sendMessage(ChatColor.RED + "Invalid Block: " + ChatColor.GOLD + args[0]);
            return;
        }

        Biome biome;
        try {
            biome = Biome.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid Biome: " + ChatColor.GOLD + args[1]);
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius of: " + ChatColor.GOLD + args[2]);
            return;
        }

        new BlockUpdateOperation(radius, player, true, block -> block.getType() == material ? new BiomeUpdate(block.getLocation(), biome) : null, (blockCount, timeFormatted) -> {
            if (player.isOnline())
                player.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.GOLD + blockCount + ChatColor.GREEN
                        + " biome sections over " + ChatColor.GOLD + timeFormatted + "s" + ChatColor.GREEN
                        + "! That count doesn't represent the amount of blocks affect." +
                        " You will need to move away or relog to see changes!");
        });
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.stream(Material.values()).map(material -> material.name().toLowerCase()).toList();
            case 2 -> Arrays.stream(Biome.values()).map(biome -> biome.name().toLowerCase()).toList();
            default -> new ArrayList<>();
        };
    }

    private record BiomeUpdate(Location location, Biome biome) implements DistributedTask.Workload {
        @Override
        public int compute() {

            int updates = 0;
            for (int x = -2; x < 2; x++)
                for (int y = -2; y < 2; y++)
                    for (int z = -2; z < 2; z++) {
                        Block block = location.clone().add(x, y, z).getBlock();
                        if (!block.getChunk().isLoaded())
                            block.getChunk().load(false);

                        if (block.getBiome() != biome) {
                            block.setBiome(biome);
                            updates++;
                        }
                    }

            return updates;
        }
    }
}
