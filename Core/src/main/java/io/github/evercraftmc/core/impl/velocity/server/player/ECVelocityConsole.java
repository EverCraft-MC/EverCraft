package io.github.evercraftmc.core.impl.velocity.server.player;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import org.jetbrains.annotations.NotNull;

public class ECVelocityConsole implements ECConsole {
    protected final ConsoleCommandSource handle;

    public ECVelocityConsole(ConsoleCommandSource handle) {
        this.handle = handle;
    }

    public ConsoleCommandSource getHandle() {
        return this.handle;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.handle.sendMessage(ECComponentFormatter.stringToComponent(message));
    }
}