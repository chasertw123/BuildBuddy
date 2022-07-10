package com.chasemc.buildbuddy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class BuildModeManager {

    private static final Set<UUID> buildMoveActive = new HashSet<>();
    private static final List<Material> materials = new ArrayList<>();

    public static boolean isActive(Player player) {
        return buildMoveActive.contains(player.getUniqueId());
    }

    public static void toggle(Player player) {
        if (BuildModeManager.isActive(player)) {
            BuildModeManager.disable(player);
            return;
        }

        BuildModeManager.enable(player);
    }

    public static void enable(Player player) {
        if (isActive(player))
            return;

        buildMoveActive.add(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Build Mode: " + ChatColor.GREEN + "ENABLED");
    }

    public static void disable(Player player) {
        if (!isActive(player))
            return;

        buildMoveActive.remove(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Build Mode: " + ChatColor.RED + "DISABLED");
    }

    public static void remove(Player player) {
        buildMoveActive.remove(player.getUniqueId());
    }

    public static void clear() {
        buildMoveActive.clear();
    }

    public static List<Player> getPlayersInBuildMode() {
        return buildMoveActive.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public static void initializeMaterials() {
        Main.getInstance().getConfig().getStringList("buildmode-blocks").forEach(s -> {
            Material material = Material.matchMaterial(s);
            if (material == null) {
                Main.getInstance().getLogger().log(Level.CONFIG, s + " is not a valid block type!");
                return;
            }

            materials.add(material);
        });
    }

    public static boolean containsMaterial(Material material) {
        return materials.contains(material);
    }

    public static void addMaterial(Material material) {
        if (materials.contains(material))
            return;

        materials.add(material);
        Main.getInstance().getConfig().set("buildmode-blocks", materials.stream().map(Enum::name).toList());
        Main.getInstance().saveConfig();
    }

    public static void removeMaterial(Material material) {
        if (!materials.contains(material))
            return;

        materials.remove(material);
        Main.getInstance().getConfig().set("buildmode-blocks", materials.stream().map(Enum::name).toList());
        Main.getInstance().saveConfig();
    }

    public static Material materialToBlockMaterial(Material material) {
        return switch (material) {
            case COCOA_BEANS -> Material.COCOA;
            case WHEAT_SEEDS -> Material.WHEAT;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS, MELON_SLICE -> Material.MELON_STEM;
            case SWEET_BERRIES -> Material.SWEET_BERRY_BUSH;
            default -> material;
        };
    }
}
