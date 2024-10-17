package io.github.evercraftmc.core.api.server;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface ECServer {
    public @NotNull ECPlugin getPlugin();

    public @NotNull String getMinecraftVersion();

    public @NotNull String getSoftwareVersion();

    public @NotNull ECEnvironment getEnvironment();

    public @NotNull ECEnvironmentType getEnvironmentType();

    public @NotNull @Unmodifiable Collection<? extends ECPlayer> getPlayers();

    public @Nullable ECPlayer getPlayer(@NotNull UUID uuid);

    public @Nullable ECPlayer getPlayer(@NotNull String name);

    public @NotNull @Unmodifiable Collection<? extends ECPlayer> getOnlinePlayers();

    public @Nullable ECPlayer getOnlinePlayer(@NotNull UUID uuid);

    public @Nullable ECPlayer getOnlinePlayer(@NotNull String name);

    public @NotNull ECConsole getConsole();

    public default void broadcastMessage(@NotNull String message) {
        this.broadcastMessage(message, false);
    }

    public default void broadcastMessage(@NotNull String message, boolean includeConsole) {
        if (includeConsole) {
            this.getConsole().sendMessage(message);
        }

        for (ECPlayer player : this.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public @NotNull ECCommandManager getCommandManager();

    public @NotNull ECEventManager getEventManager();

    public @NotNull ECScheduler getScheduler();
}