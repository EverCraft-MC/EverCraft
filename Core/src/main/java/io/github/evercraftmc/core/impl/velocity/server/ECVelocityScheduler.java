package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.scheduler.ScheduledTask;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.api.server.ECScheduler;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class ECVelocityScheduler implements ECScheduler {
    public static class ECVelocityTask implements ECTask {
        protected final @NotNull ScheduledTask handle;

        public ECVelocityTask(@NotNull ScheduledTask handle) {
            this.handle = handle;
        }

        public @NotNull ScheduledTask getHandle() {
            return this.handle;
        }

        public void cancel() {
            this.handle.cancel();
        }
    }

    protected final @NotNull ECVelocityServer server;

    public ECVelocityScheduler(@NotNull ECVelocityServer server) {
        this.server = server;
    }

    public @NotNull ECProxyServer getServer() {
        return this.server;
    }

    @Override
    public @NotNull ECVelocityScheduler.ECVelocityTask runTask(@NotNull Runnable task) {
        return new ECVelocityScheduler.ECVelocityTask(this.server.getHandle().getScheduler().buildTask(this.server.getPlugin().getHandle(), task).schedule());
    }

    @Override
    public @NotNull ECVelocityScheduler.ECVelocityTask runTaskLater(@NotNull Runnable task, int ticks) {
        return new ECVelocityScheduler.ECVelocityTask(this.server.getHandle().getScheduler().buildTask(this.server.getPlugin().getHandle(), task).delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public @NotNull ECVelocityScheduler.ECVelocityTask runTaskRepeat(@NotNull Runnable task, int delay, int ticks) {
        return new ECVelocityScheduler.ECVelocityTask(this.server.getHandle().getScheduler().buildTask(this.server.getPlugin().getHandle(), task).delay(delay * 50L, TimeUnit.MILLISECONDS).delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule());
    }
}