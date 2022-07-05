package com.chasemc.buildbuddy.cmds.impls;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import com.chasemc.buildbuddy.utils.DistributedTask;
import com.chasemc.buildbuddy.utils.BlockUpdateOperation;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnwaterLogCommand extends PlayerCommand {

    public UnwaterLogCommand() {
        super("unwaterlog", "buildbuddy.unwaterlog");
    }

    @Override
    protected void onCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid Usage! /unwaterlog <radius>");
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius of: " + ChatColor.GOLD + args[0]);
            return;
        }

        new BlockUpdateOperation(radius, player, UnWaterLogWorkload::new);
    }

    @Override
    protected List<String> onTabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }

    private record UnWaterLogWorkload(Block block) implements DistributedTask.Workload {

        @Override
        public int compute() {
            if (!(block.getBlockData() instanceof Waterlogged waterlogged) || !waterlogged.isWaterlogged())
                return 0;

            waterlogged.setWaterlogged(false);
            block.setBlockData(waterlogged, false);
            return 1;
        }
    }
}
