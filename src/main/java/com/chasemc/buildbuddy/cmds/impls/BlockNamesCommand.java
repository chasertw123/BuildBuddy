package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import com.chasemc.buildbuddy.BlockNameCopierManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockNamesCommand extends PlayerCommand {

    public BlockNamesCommand() {
        super("blocknames", "buildbuddy.blocknames");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        if (args.length > 0) {
            player.sendMessage(ChatColor.RED + "Invalid Usage! /blocknames");
            return;
        }

        if (BlockNameCopierManager.isCopying(player)) {
            if (BlockNameCopierManager.count(player) <= 0) {
                BlockNameCopierManager.stop(player);
                player.sendMessage(ChatColor.RED + "You did not interact with any blocks!");
                return;
            }

            BlockNameCopierManager.sendBlocksAsMessage(player);
            BlockNameCopierManager.stop(player);
        } else {
            BlockNameCopierManager.start(player);
            player.sendMessage(ChatColor.GREEN + "Right click blocks to add them to a list, then use the command again to get a copiable list.");
        }
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return null;
    }
}
