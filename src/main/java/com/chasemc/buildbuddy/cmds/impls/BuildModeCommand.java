package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import com.chasemc.buildbuddy.BuildModeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BuildModeCommand extends PlayerCommand {

    public BuildModeCommand() {
        super("buildmode", "buildbuddy.buildmode");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            BuildModeManager.toggle(player);
            return;
        }

        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(ChatColor.YELLOW + "/buildmode " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Toggle build mode on and off");
                player.sendMessage(ChatColor.YELLOW + "/buildmode help " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Bring up this information");
                player.sendMessage(ChatColor.YELLOW + "/buildmode on " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Turn on build mode");
                player.sendMessage(ChatColor.YELLOW + "/buildmode off " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Turn off build mode");
                player.sendMessage(ChatColor.YELLOW + "/buildmode add " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Add the item in your hand to the special block handler");
                player.sendMessage(ChatColor.YELLOW + "/buildmode remove " + ChatColor.WHITE + " - " + ChatColor.GREEN + "Remove the item in your hand from the special block handler");
                return;
            }

            else if (args[0].equalsIgnoreCase("on")) {
                if (BuildModeManager.isActive(player)) {
                    player.sendMessage(ChatColor.YELLOW + "Build Mode is already enabled!");
                    return;
                }

                BuildModeManager.enable(player);
                return;
            }

            else if (args[0].equalsIgnoreCase("off")) {
                if (!BuildModeManager.isActive(player)) {
                    player.sendMessage(ChatColor.YELLOW + "Build Mode is already disabled!");
                    return;
                }

                BuildModeManager.disable(player);
                return;
            }

            else if (args[0].equalsIgnoreCase("add")) {
                Material material = player.getInventory().getItemInMainHand().getType();
                if (material == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You need to have an item in your hand!");
                    return;
                }

                if (BuildModeManager.containsMaterial(material)) {
                    player.sendMessage(ChatColor.RED + "That item is already added to special block handler!");
                    return;
                }

                BuildModeManager.addMaterial(material);
                player.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + material.name().toLowerCase().replace('_', ' ') + ChatColor.GREEN + " to the special block handler!");
                return;
            }

            else if (args[0].equalsIgnoreCase("remove")) {
                Material material = player.getInventory().getItemInMainHand().getType();
                if (material == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You need to have an item in your hand!");
                    return;
                }

                if (!BuildModeManager.containsMaterial(material)) {
                    player.sendMessage(ChatColor.RED + "That item isn't currently handled by the special block handler!");
                    return;
                }

                BuildModeManager.removeMaterial(material);
                player.sendMessage(ChatColor.GREEN + "Removed " + ChatColor.YELLOW + material.name().toLowerCase().replace('_', ' ') + ChatColor.GREEN + " from the special block handler!");
                return;
            }
        }

        player.sendMessage(ChatColor.RED + "Unknown usage!");
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return Arrays.asList("help", "on", "off", "add", "remove");
    }
}
