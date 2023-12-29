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

public interface ECServer {
    @NotNull ECPlugin getPlugin();

    @NotNull String getMinecraftVersion();

    @NotNull String getSoftwareVersion();

    @NotNull ECEnvironment getEnvironment();

    @NotNull ECEnvironmentType getEnvironmentType();

    @NotNull Collection<? extends ECPlayer> getPlayers();

    @Nullable ECPlayer getPlayer(@NotNull UUID uuid);

    @Nullable ECPlayer getPlayer(@NotNull String name);

    @NotNull Collection<? extends ECPlayer> getOnlinePlayers();

    @Nullable ECPlayer getOnlinePlayer(@NotNull UUID uuid);

    @Nullable ECPlayer getOnlinePlayer(@NotNull String name);

    @NotNull ECConsole getConsole();

    default void broadcastMessage(@NotNull String message) {
        for (ECPlayer player : this.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    @NotNull ECCommandManager getCommandManager();

    @NotNull ECEventManager getEventManager();

    @NotNull ECScheduler getScheduler();
}