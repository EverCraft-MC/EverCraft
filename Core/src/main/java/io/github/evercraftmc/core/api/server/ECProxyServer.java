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

    @Override
    public @NotNull Collection<? extends ECProxyPlayer> getPlayers();

    @Override
    public ECProxyPlayer getPlayer(@NotNull UUID uuid);

    @Override
    public ECProxyPlayer getPlayer(@NotNull String name);

    @Override
    public @NotNull Collection<? extends ECProxyPlayer> getOnlinePlayers();

    @Override
    public ECProxyPlayer getOnlinePlayer(@NotNull UUID uuid);

    @Override
    public ECProxyPlayer getOnlinePlayer(@NotNull String name);

    public @NotNull String getAllMinecraftVersions();

    public @NotNull @Unmodifiable List<Server> getServers();

    public @Nullable Server getServer(@NotNull String name);

    public @NotNull Server getDefaultServer();

    public @NotNull Server getFallbackServer();
}