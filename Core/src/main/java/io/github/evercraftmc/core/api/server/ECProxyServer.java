package io.github.evercraftmc.core.api.server;

import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface ECProxyServer extends ECServer {
    @Override
    public @NotNull Collection<? extends ECProxyPlayer> getPlayers();

    @Override
    public @Nullable ECProxyPlayer getPlayer(@NotNull UUID uuid);

    @Override
    public @Nullable ECProxyPlayer getPlayer(@NotNull String name);

    @Override
    public @NotNull Collection<? extends ECProxyPlayer> getOnlinePlayers();

    @Override
    public @Nullable ECProxyPlayer getOnlinePlayer(@NotNull UUID uuid);

    @Override
    public @Nullable ECProxyPlayer getOnlinePlayer(@NotNull String name);

    public @NotNull String getAllMinecraftVersions();

    public @NotNull @Unmodifiable List<ECServerInfo> getServers();

    public @Nullable ECServerInfo getServer(@NotNull String name);

    public @NotNull ECServerInfo getDefaultServer();

    public @NotNull ECServerInfo getFallbackServer();
}