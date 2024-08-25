package io.github.evercraftmc.core.api.events.proxy.player;

import io.github.evercraftmc.core.api.events.ECEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerProxyPingEvent extends ECEvent {
    protected @NotNull String motd;
    protected boolean centerMotd;

    protected @Nullable BufferedImage favicon;

    protected int onlinePlayers;
    protected int maxPlayers;
    protected @NotNull Map<UUID, String> players;

    protected final @Nullable InetAddress address;
    protected final @Nullable InetSocketAddress serverAddress;

    public PlayerProxyPingEvent(@NotNull String motd, boolean centerMotd, @Nullable BufferedImage favicon, int onlinePlayers, int maxPlayers, @NotNull Map<UUID, String> players, @Nullable InetAddress address, @Nullable InetSocketAddress serverAddress) {
        this.motd = motd;
        this.centerMotd = centerMotd;

        this.favicon = favicon;

        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.players = players;

        this.address = address;
        this.serverAddress = serverAddress;
    }

    public @NotNull String getMotd() {
        return this.motd;
    }

    public void setMotd(@NotNull String motd) {
        this.motd = motd;
    }

    public boolean getCenterMotd() {
        return this.centerMotd;
    }

    public void setCenterMotd(boolean centerMotd) {
        this.centerMotd = centerMotd;
    }

    public @Nullable BufferedImage getFavicon() {
        return this.favicon;
    }

    public void setFavicon(@Nullable BufferedImage favicon) {
        this.favicon = favicon;
    }

    public int getOnlinePlayers() {
        return this.onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public @NotNull Map<UUID, String> getPlayers() {
        return this.players;
    }

    public void setPlayers(@NotNull Map<UUID, String> players) {
        this.players = Map.copyOf(players);
    }

    public @Nullable InetAddress getAddress() {
        return this.address;
    }

    public @Nullable InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }
}