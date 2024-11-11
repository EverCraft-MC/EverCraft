package io.github.evercraftmc.core.api.events.proxy.player;

import io.github.evercraftmc.core.api.events.ECCancelableEvent;
import io.github.evercraftmc.core.api.server.ECServerInfo;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerProxyKickEvent extends ECCancelableEvent {
    protected final @NotNull ECPlayer player;

    protected final @NotNull ECServerInfo fromServer;
    protected @NotNull ECServerInfo toServer;

    protected @NotNull String kickMessage;

    public PlayerProxyKickEvent(@NotNull ECPlayer player, @NotNull ECServerInfo fromServer, @NotNull ECServerInfo toServer, @NotNull String kickMessage) {
        this.player = player;

        this.fromServer = fromServer;
        this.toServer = toServer;

        this.kickMessage = kickMessage;
    }

    public @NotNull ECPlayer getPlayer() {
        return this.player;
    }

    public @NotNull ECServerInfo getFromServer() {
        return this.fromServer;
    }

    public @NotNull ECServerInfo getToServer() {
        return this.toServer;
    }

    public void setToServer(@NotNull ECServerInfo toServer) {
        this.toServer = toServer;
    }

    public @NotNull String getKickMessage() {
        return this.kickMessage;
    }

    public void setKickMessage(@NotNull String kickMessage) {
        this.kickMessage = kickMessage;
    }
}