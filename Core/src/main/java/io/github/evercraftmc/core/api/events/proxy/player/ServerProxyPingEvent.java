package io.github.evercraftmc.core.api.events.proxy.player;

import io.github.evercraftmc.core.api.events.player.ServerPingEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerProxyPingEvent extends ServerPingEvent {
    protected final @Nullable InetAddress address;
    protected final @Nullable InetSocketAddress serverAddress;

    public ServerProxyPingEvent(@NotNull String motd, boolean centerMotd, @Nullable BufferedImage favicon, int onlinePlayers, int maxPlayers, @NotNull Map<UUID, String> players, @Nullable InetAddress address, @Nullable InetSocketAddress serverAddress) {
        super(motd, centerMotd, favicon, onlinePlayers, maxPlayers, players);

        this.address = address;
        this.serverAddress = serverAddress;
    }

    public @Nullable InetAddress getAddress() {
        return this.address;
    }

    public @Nullable InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }
}