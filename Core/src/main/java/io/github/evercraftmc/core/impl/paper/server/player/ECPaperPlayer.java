package io.github.evercraftmc.core.impl.paper.server.player;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.paper.server.ECPaperServer;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECPaperPlayer implements ECPlayer {
    protected final ECPaperServer parent;
    protected final Player handle;

    protected final @NotNull UUID uuid;
    protected final @NotNull String name;

    protected @NotNull String displayName;

    public ECPaperPlayer(@NotNull ECPlayerData.Player data) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? "&r" + data.prefix + "&r " : "&r") + data.nickname + "&r");

        this.parent = null;
        this.handle = null;
    }

    public ECPaperPlayer(@NotNull ECPlayerData.Player data, @NotNull ECPaperServer parent, @NotNull Player handle) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? "&r" + data.prefix + "&r " : "&r") + data.nickname + "&r");

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
        return ECComponentFormatter.componentToString(this.handle.displayName());
    }

    @Override
    public void setOnlineDisplayName(@NotNull String displayName) {
        this.setDisplayName(displayName);

        this.handle.displayName(ECComponentFormatter.stringToComponent(displayName));
        this.handle.customName(ECComponentFormatter.stringToComponent(displayName));
        this.handle.playerListName(ECComponentFormatter.stringToComponent(displayName));
    }

    @Override
    public @Nullable InetAddress getAddress() {
        InetSocketAddress socketAddress = this.handle.getAddress();
        if (socketAddress != null) {
            return socketAddress.getAddress();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable InetSocketAddress getServerAddress() {
        return this.handle.getVirtualHost();
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
        this.handle.kick(ECComponentFormatter.stringToComponent(message));
    }
}