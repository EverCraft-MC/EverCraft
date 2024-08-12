package io.github.evercraftmc.core.api.server.player;

import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECConsole extends ECPlayer {
    @Override
    public default @NotNull UUID getUuid() {
        return new UUID(0, 0);
    }

    @Override
    public default @NotNull String getName() {
        return "Console";
    }

    @Override
    public default @NotNull String getDisplayName() {
        return ECTextFormatter.translateColors("&l&4Console");
    }

    @Override
    public default void setDisplayName(@NotNull String displayName) {
        throw new UnsupportedOperationException("Player is console");
    }

    @Override
    public default boolean isOnline() {
        return true;
    }

    @Override
    public default @NotNull String getOnlineDisplayName() {
        return ECTextFormatter.translateColors("&l&4Console");
    }

    @Override
    public default void setOnlineDisplayName(@NotNull String displayName) {
        throw new UnsupportedOperationException("Player is console");
    }

    @Override
    public default @Nullable InetAddress getAddress() {
        return null;
    }

    @Override
    public default @Nullable InetSocketAddress getServerAddress() {
        return null;
    }

    @Override
    public default void kick(@NotNull String message) {
        throw new UnsupportedOperationException("Player is console");
    }
}