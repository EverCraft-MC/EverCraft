package io.github.evercraftmc.core.impl.paper.server.player;

import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.impl.paper.util.ECPaperComponentFormatter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ECPaperConsole implements ECConsole {
    protected final @NotNull CommandSender handle;

    public ECPaperConsole(@NotNull CommandSender handle) {
        this.handle = handle;
    }

    public @NotNull CommandSender getHandle() {
        return this.handle;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.handle.sendMessage(ECPaperComponentFormatter.stringToComponent(message));
    }
}