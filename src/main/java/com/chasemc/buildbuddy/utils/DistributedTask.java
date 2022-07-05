package com.chasemc.buildbuddy.utils;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class DistributedTask extends BukkitRunnable {

    private static final double MAX_MILLIS_PER_TICK = 40;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    private final Deque<Workload> workloads = new ArrayDeque<>();


    private final Consumer<Long> action;

    private long blocksUpdated = 0;

    public DistributedTask(Consumer<Long> action) {
        this.action = action;
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Workload nextLoad;
        while (System.nanoTime() <= stopTime && (nextLoad = this.workloads.poll()) != null)
            blocksUpdated += nextLoad.compute();

        if (workloads.isEmpty()) {
            action.accept(blocksUpdated);
            this.cancel();
        }
    }

    public void addWorkLoad(Workload workload) {
        this.workloads.add(workload);
    }

    public interface Workload {

        int compute();

    }
}
