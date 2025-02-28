package io.github.evercraftmc.core.impl.paper.server.player;

import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class ECPaperConsole implements ECConsole {
    protected final ConsoleCommandSender handle;

    public ECPaperConsole(ConsoleCommandSender handle) {
        this.handle = handle;
    }

    public ConsoleCommandSender getHandle() {
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