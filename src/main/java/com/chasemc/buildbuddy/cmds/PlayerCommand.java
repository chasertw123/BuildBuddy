package com.chasemc.buildbuddy.cmds;

import com.chasemc.buildbuddy.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class PlayerCommand implements TabExecutor {

    private final String command;
    private final String permission;

    public PlayerCommand(String command, String permission) {
        this.command = command;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ChatColor.RED + "That command is only usable by players!");
            return true;
        }

        if (!player.hasPermission(permission)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
            return true;
        }

        this.onCommand(player, strings);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player))
            return null;

        return this.onTabComplete(player, strings);
    }

    protected abstract void onCommand(Player player, String[] args);

    protected abstract List<String> onTabComplete(Player player, String[] args);

    public static void registerCommand(PlayerCommand playerCommand) {
        PluginCommand pluginCommand = Main.getInstance().getCommand(playerCommand.command);
        if (pluginCommand == null)
            return;

        pluginCommand.setExecutor(playerCommand);
        pluginCommand.setTabCompleter(playerCommand);
    }
}
