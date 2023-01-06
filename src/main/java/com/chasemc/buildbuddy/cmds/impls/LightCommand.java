package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;

import java.util.ArrayList;
import java.util.List;

public class LightCommand extends PlayerCommand {

    public LightCommand() {
        super("light", "buildbuddy.light");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Invalid Usage! /light [level]");
            return;
        }

        int level = 15;
        if (args.length == 1) {
            try {
                level = Integer.parseInt(args[0]);
                if (level > 15 || level < 0) {
                    throw new NumberFormatException("");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid light level of: " + ChatColor.GOLD + args[0]);
                return;
            }
        }

        try {
            ItemStack item = new ItemStack(Material.LIGHT);
            BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();

            assert meta != null;
            Light state = (Light) meta.getBlockData(Material.LIGHT);
            state.setLevel(level);
            meta.setBlockData(state);
            item.setItemMeta(meta);

            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "Added a level " + ChatColor.YELLOW + level + ChatColor.GREEN + " light block to your inventory!");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Unable to use command in this version! Please update to a newer version of Spigot/Paper!");
        }
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
