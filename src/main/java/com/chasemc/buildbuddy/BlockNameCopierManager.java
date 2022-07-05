package com.chasemc.buildbuddy;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class BlockNameCopierManager {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Map<UUID, List<Material>> activeCopies = new HashMap<>();

    public static boolean isCopying(Player player) {
        return activeCopies.containsKey(player.getUniqueId());
    }

    public static void start(Player player) {
        activeCopies.put(player.getUniqueId(), new ArrayList<>());
    }

    public static void stop(Player player) {
        activeCopies.remove(player.getUniqueId());
    }

    public static boolean addBlock(Player player, Block block) {
        if (activeCopies.get(player.getUniqueId()).contains(block.getType()))
            return false;

        activeCopies.get(player.getUniqueId()).add(block.getType());
        return true;
    }

    public static int count(Player player) {
        return activeCopies.get(player.getUniqueId()).size();
    }

    public static void sendBlocksAsMessage(Player player) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activeCopies.get(player.getUniqueId()).size(); i++) {
            if (i > 0)
                sb.append(",");

            sb.append(activeCopies.get(player.getUniqueId()).get(i).name().toLowerCase());
        }

        TextComponent message = new TextComponent(new ComponentBuilder()
                .append("[" + TIME_FORMAT.format(new Date()) + "] ").color(ChatColor.GRAY)
                .append("Click to copy Block Names!").color(ChatColor.of(new Color(255, 0, 174)))
                .create());

        message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, sb.toString()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + "CLICK TO COPY"))));

        player.spigot().sendMessage(message);
    }
}
