package io.github.evercraftmc.core.api.server;

import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface ECProxyServer extends ECServer {
    public record Server(@NotNull String name, @NotNull InetSocketAddress address) {
    }

    public @NotNull Collection<? extends ECProxyPlayer> getOnlinePlayers();

    public ECProxyPlayer getOnlinePlayer(@NotNull UUID uuid);

    public ECProxyPlayer getOnlinePlayer(@NotNull String name);

    public @NotNull String getAllMinecraftVersions();

    public @NotNull String getDefaultServer();

    public @NotNull String getFallbackServer();

    public @NotNull @Unmodifiable List<Server> getServers();

    public @Nullable Server getServer(@NotNull String name);
}