package io.github.evercraftmc.core.impl.paper.server.player;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.paper.util.ECPaperComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECPaperPlayer implements ECPlayer {
    protected final Player handle;

    protected final @NotNull UUID uuid;
    protected final @NotNull String name;

    protected @NotNull String displayName;

    public ECPaperPlayer(@NotNull ECPlayerData.Player data) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? data.prefix + "&r " : "&r") + data.displayName + "&r");

        this.handle = null;
    }

    public ECPaperPlayer(@NotNull ECPlayerData.Player data, @NotNull Player handle) {
        this.uuid = data.uuid;
        this.name = data.name;

        this.displayName = ECTextFormatter.translateColors((data.prefix != null ? data.prefix + "&r " : "&r") + data.displayName + "&r");

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
    public @NotNull String getOnlineDisplayName() {
        return ECPaperComponentFormatter.componentToString(this.handle.displayName());
    }

    @Override
    public void setOnlineDisplayName(@NotNull String displayName) {
        this.setDisplayName(displayName);

        this.handle.customName(ECPaperComponentFormatter.stringToComponent(displayName));
        this.handle.displayName(ECPaperComponentFormatter.stringToComponent(displayName));
        this.handle.playerListName(ECPaperComponentFormatter.stringToComponent(displayName));
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
    public @Nullable String getServer() {
        throw new UnsupportedOperationException("Server is backend");
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.handle.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.handle.sendMessage(ECPaperComponentFormatter.stringToComponent(message));
    }

    @Override
    public void kick(@NotNull String message) {
        this.handle.kick(ECPaperComponentFormatter.stringToComponent(message));
    }
}