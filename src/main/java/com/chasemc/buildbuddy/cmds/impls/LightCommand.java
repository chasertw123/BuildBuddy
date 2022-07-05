package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LightCommand extends PlayerCommand {

    public LightCommand() {
        super("light", "buildbuddy.light");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        player.getInventory().addItem(new ItemStack(Material.LIGHT));
        player.sendMessage(ChatColor.GREEN + "Added a light block to your inventory!");
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
