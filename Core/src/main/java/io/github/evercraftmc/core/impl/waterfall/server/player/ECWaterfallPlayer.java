package io.github.evercraftmc.core.impl.waterfall.server.player;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.waterfall.util.ECWaterfallComponentFormatter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECWaterfallPlayer implements ECPlayer {
    protected final ProxiedPlayer handle;

    protected final @NotNull UUID uuid;
    protected final @NotNull String name;

    protected @NotNull String displayName;

    public ECWaterfallPlayer(@NotNull ECPlayerData.Player data) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? data.prefix + "&r " : "&r") + data.displayName + "&r");

        this.handle = null;
    }

    public ECWaterfallPlayer(@NotNull ECPlayerData.Player data, @NotNull ProxiedPlayer handle) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? data.prefix + "&r " : "&r") + data.displayName + "&r");

        this.handle = handle;
    }

    public ProxiedPlayer getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    @Override
    public @NotNull String getOnlineDisplayName() {
        return this.handle.getDisplayName();
    }

    @Override
    public void setOnlineDisplayName(@NotNull String displayName) {
        this.setDisplayName(displayName);

        this.handle.setDisplayName(displayName);
    }

    @Override
    public @Nullable InetAddress getAddress() {
        SocketAddress socketAddress = this.handle.getPendingConnection().getSocketAddress();
        if (socketAddress == null) {
            return null;
        }

        if (socketAddress instanceof InetSocketAddress address) {
            return address.getAddress();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable InetSocketAddress getServerAddress() {
        return this.handle.getPendingConnection().getVirtualHost();
    }

    @Override
    public @Nullable String getServer() {
        return this.handle.getServer() != null ? this.handle.getServer().getInfo().getName().toLowerCase() : null;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.handle.sendMessage(ECWaterfallComponentFormatter.stringToComponent(message));
    }

    @Override
    public void kick(@NotNull String message) {
        this.handle.disconnect(ECWaterfallComponentFormatter.stringToComponent(message));
    }
}