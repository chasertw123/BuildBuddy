package com.chasemc.buildbuddy.listeners;

import com.chasemc.buildbuddy.BlockNameCopierManager;
import com.chasemc.buildbuddy.BuildModeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("unused")
public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BuildModeManager.remove(event.getPlayer());

        if (BlockNameCopierManager.isCopying(event.getPlayer()))
            BlockNameCopierManager.stop(event.getPlayer());
    }

}
