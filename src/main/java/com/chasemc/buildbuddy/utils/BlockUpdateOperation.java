package com.chasemc.buildbuddy.utils;

import com.chasemc.buildbuddy.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BlockUpdateOperation extends BukkitRunnable {

    private static final double MAX_MILLIS_PER_TICK = 35;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    private final int radius;
    private final int yBounds;
    private final boolean limitToPlayerY;
    private final Location origin;

    private final DistributedTask task;
    private final Function<Block, DistributedTask.Workload> workloadSupplier;

    int x;
    int y;
    int z;

    public BlockUpdateOperation(int radius, Player player, boolean limitToPlayerY, Function<Block, DistributedTask.Workload> workloadSupplier, BiConsumer<Long, String> onComplete) {
        this.radius = radius;
        this.limitToPlayerY = limitToPlayerY;
        this.origin = player.getLocation().clone();
        this.workloadSupplier = workloadSupplier;

        this.yBounds = (int) -(player.getLocation().getY() + 64);

        this.x = -radius;
        this.z = -radius;
        this.y = Math.max(-64, -radius);

        long startTime = System.currentTimeMillis();

        task = new DistributedTask(blocks -> {
            String seconds = String.format("%.3f", (System.currentTimeMillis() - startTime) / 1000D);
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> onComplete.accept(blocks, seconds));
        });

        player.sendMessage(ChatColor.GRAY + "Starting operation...");
        this.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public BlockUpdateOperation(int radius, Player player, Function<Block, DistributedTask.Workload> workloadSupplier) {
        this(radius, player, true, workloadSupplier, (blockCount, timeString) -> {
            if (player.isOnline())
                player.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.GOLD + blockCount + ChatColor.GREEN
                        + " blocks over " + ChatColor.GOLD + timeString + "s" + ChatColor.GREEN + "!");
        });
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;
        while (x < radius) {
            while (z < radius) {
                while (y < (limitToPlayerY ? 0 : radius)) {
                    if (!(x * x + y * y + z * z < radius * radius)) {
                        y += 1;
                        continue;
                    }

                    DistributedTask.Workload workload = workloadSupplier.apply(origin.clone().add(x, y, z).getBlock());
                    if (workload != null)
                        task.addWorkLoad(workload);

                    y += 1;
                }

                z += 1;
                y = Math.max(yBounds, -radius);
                if (System.nanoTime() >= stopTime)
                    return;
            }

            x += 1;
            z = -radius;
            if (System.nanoTime() >= stopTime)
                return;
        }

        task.runTaskTimer(Main.getInstance(), 0L, 1L);
        this.cancel();
    }
}
