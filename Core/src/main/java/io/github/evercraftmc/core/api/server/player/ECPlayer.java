package io.github.evercraftmc.core.api.server.player;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECPlayer {
    public @NotNull UUID getUuid();

    public @NotNull String getName();

    public @NotNull String getDisplayName();

    public void setDisplayName(@NotNull String displayName);

    public boolean isOnline();

    public @NotNull String getOnlineDisplayName();

    public void setOnlineDisplayName(@NotNull String displayName);

    public @Nullable InetAddress getAddress();

    public @Nullable InetSocketAddress getServerAddress();

    public boolean hasPermission(@NotNull String permission);

    public void sendMessage(@NotNull String message);

    public void kick(@NotNull String message);
}