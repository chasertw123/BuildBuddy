package com.chasemc.buildbuddy;

import com.chasemc.buildbuddy.cmds.PlayerCommand;
import com.chasemc.buildbuddy.cmds.impls.*;
import com.chasemc.buildbuddy.listeners.BlockInteractListener;
import com.chasemc.buildbuddy.listeners.BlockUpdatesListeners;
import com.chasemc.buildbuddy.listeners.PlayerListeners;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        this.getConfig().options().copyDefaults();
        this.saveDefaultConfig();

        BuildModeManager.initializeMaterials();

        PlayerCommand.registerCommand(new BuildModeCommand());
        PlayerCommand.registerCommand(new LightCommand());
        PlayerCommand.registerCommand(new WaterLogCommand());
        PlayerCommand.registerCommand(new UnwaterLogCommand());
        PlayerCommand.registerCommand(new BlockBiomeCommand());
        PlayerCommand.registerCommand(new BlockNamesCommand());

        this.getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        this.getServer().getPluginManager().registerEvents(new BlockUpdatesListeners(), this);
        this.getServer().getPluginManager().registerEvents(new BlockInteractListener(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static Main getInstance() {
        return instance;
    }
}
