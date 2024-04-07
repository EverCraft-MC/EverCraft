package io.github.evercraftmc.core.impl.waterfall.server;

import io.github.evercraftmc.core.api.server.ECScheduler;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

public class ECWaterfallScheduler implements ECScheduler {
    public static class ECBungeeTask implements ECTask {
        protected final @NotNull ScheduledTask handle;

        public ECBungeeTask(@NotNull ScheduledTask handle) {
            this.handle = handle;
        }

        public @NotNull ScheduledTask getHandle() {
            return this.handle;
        }

        public void cancel() {
            this.handle.cancel();
        }
    }

    protected final @NotNull ECWaterfallServer server;

    public ECWaterfallScheduler(@NotNull ECWaterfallServer server) {
        this.server = server;
    }

    public @NotNull ECWaterfallServer getServer() {
        return this.server;
    }

    @Override
    public @NotNull ECBungeeTask runTask(@NotNull Runnable task) {
        return this.runTaskAsync(task);
    }

    @Override
    public @NotNull ECBungeeTask runTaskAsync(@NotNull Runnable task) {
        return new ECBungeeTask(this.server.getHandle().getScheduler().runAsync((Plugin) this.server.getPlugin().getHandle(), task));
    }

    @Override
    public @NotNull ECBungeeTask runTaskLater(@NotNull Runnable task, int ticks) {
        return this.runTaskLaterAsync(task, ticks);
    }

    @Override
    public @NotNull ECBungeeTask runTaskLaterAsync(@NotNull Runnable task, int ticks) {
        return new ECBungeeTask(this.server.getHandle().getScheduler().schedule((Plugin) this.server.getPlugin().getHandle(), task, ticks * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public @NotNull ECBungeeTask runTaskRepeat(@NotNull Runnable task, int delay, int ticks) {
        return this.runTaskRepeatAsync(task, delay, ticks);
    }

    @Override
    public @NotNull ECBungeeTask runTaskRepeatAsync(@NotNull Runnable task, int delay, int ticks) {
        return new ECBungeeTask(this.server.getHandle().getScheduler().schedule((Plugin) this.server.getPlugin().getHandle(), task, delay * 50L, ticks * 50L, TimeUnit.MILLISECONDS));
    }
}