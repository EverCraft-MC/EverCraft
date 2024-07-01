package io.github.evercraftmc.core.api.server;

import org.jetbrains.annotations.NotNull;

public interface ECScheduler {
    public interface ECTask {
        public void cancel();
    }

    public @NotNull ECTask runTask(@NotNull Runnable task);

    public @NotNull ECTask runTaskAsync(@NotNull Runnable task);

    public @NotNull ECTask runTaskLater(@NotNull Runnable task, int ticks);

    public @NotNull ECTask runTaskLaterAsync(@NotNull Runnable task, int ticks);

    public @NotNull ECTask runTaskRepeat(@NotNull Runnable task, int delay, int ticks);

    public @NotNull ECTask runTaskRepeatAsync(@NotNull Runnable task, int delay, int ticks);
}