package io.github.evercraftmc.core.impl.paper.server;

import io.github.evercraftmc.core.api.server.ECScheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class ECPaperScheduler implements ECScheduler {
    public static class ECPaperTask implements ECTask {
        protected final @NotNull BukkitTask handle;

        public ECPaperTask(@NotNull BukkitTask handle) {
            this.handle = handle;
        }

        public @NotNull BukkitTask getHandle() {
            return this.handle;
        }

        public void cancel() {
            this.handle.cancel();
        }
    }

    protected final @NotNull ECPaperServer server;

    public ECPaperScheduler(@NotNull ECPaperServer server) {
        this.server = server;
    }

    public @NotNull ECPaperServer getServer() {
        return this.server;
    }

    @Override
    public @NotNull ECPaperScheduler.ECPaperTask runTask(@NotNull Runnable task) {
        return new ECPaperTask(this.server.getHandle().getScheduler().runTaskAsynchronously((Plugin) this.server.getPlugin().getHandle(), task));
    }

    @Override
    public @NotNull ECPaperScheduler.ECPaperTask runTaskLater(@NotNull Runnable task, int ticks) {
        return new ECPaperTask(this.server.getHandle().getScheduler().runTaskLaterAsynchronously((Plugin) this.server.getPlugin().getHandle(), task, ticks));
    }

    @Override
    public @NotNull ECPaperScheduler.ECPaperTask runTaskRepeat(@NotNull Runnable task, int delay, int ticks) {
        return new ECPaperTask(this.server.getHandle().getScheduler().runTaskTimerAsynchronously((Plugin) this.server.getPlugin().getHandle(), task, delay, ticks));
    }
}