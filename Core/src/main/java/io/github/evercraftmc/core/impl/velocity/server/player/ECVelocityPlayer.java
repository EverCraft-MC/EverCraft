package io.github.evercraftmc.core.impl.velocity.server.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.velocity.server.ECVelocityServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECVelocityPlayer implements ECProxyPlayer {
    protected final ECVelocityServer parent;
    protected final Player handle;

    protected final @NotNull UUID uuid;
    protected final @NotNull String name;

    protected @NotNull String displayName;

    public ECVelocityPlayer(@NotNull ECPlayerData.Player data) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? "&r" + data.prefix + "&r " : "&r") + data.displayName + "&r");

        this.parent = null;
        this.handle = null;
    }

    public ECVelocityPlayer(@NotNull ECPlayerData.Player data, @NotNull ECVelocityServer parent, @NotNull Player handle) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? "&r" + data.prefix + "&r " : "&r") + data.displayName + "&r");

        this.parent = parent;
        this.handle = handle;
    }

    public Player getHandle() {
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
    public boolean isOnline() {
        return this.handle != null;
    }

    @Override
    public @NotNull String getOnlineDisplayName() {
        return this.getDisplayName();
    }

    @Override
    public void setOnlineDisplayName(@NotNull String displayName) {
        this.setDisplayName(displayName);

        // TODO?
    }

    @Override
    public @Nullable InetAddress getAddress() {
        InetSocketAddress socketAddress = this.handle.getRemoteAddress();

        if (socketAddress != null) {
            return socketAddress.getAddress();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable InetSocketAddress getServerAddress() {
        return this.handle.getVirtualHost().orElse(null);
    }

    @Override
    public @Nullable ECProxyServer.Server getServer() {
        Optional<ServerConnection> server = this.handle.getCurrentServer();

        if (server.isPresent()) {
            return new ECProxyServer.Server(server.get().getServerInfo().getName(), server.get().getServerInfo().getAddress());
        } else {
            return null;
        }
    }

    @Override
    public void setServer(@NotNull ECProxyServer.Server server) {
        Optional<RegisteredServer> serverHandle = parent.getHandle().getServer(server.name());

        if (serverHandle.isPresent()) {
            this.handle.createConnectionRequest(serverHandle.get()).connectWithIndication();
        } else {
            throw new NullPointerException(server.name());
        }
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.handle.sendMessage(ECComponentFormatter.stringToComponent(message));
    }

    @Override
    public void kick(@NotNull String message) {
        this.handle.disconnect(ECComponentFormatter.stringToComponent(message));
    }
}