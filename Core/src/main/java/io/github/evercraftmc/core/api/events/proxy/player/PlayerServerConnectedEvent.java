package io.github.evercraftmc.core.api.events.proxy.player;

import io.github.evercraftmc.core.api.events.ECEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerServerConnectedEvent extends ECEvent {
    protected final @NotNull ECPlayer player;

    protected final @NotNull String targetServer;

    protected @NotNull String connectMessage;

    public PlayerServerConnectedEvent(@NotNull ECPlayer player, @NotNull String targetServer, @NotNull String connectMessage) {
        this.player = player;

        this.targetServer = targetServer;

        this.connectMessage = connectMessage;
    }

    public @NotNull ECPlayer getPlayer() {
        return this.player;
    }

    public @NotNull String getTargetServer() {
        return this.targetServer;
    }

    public @NotNull String getConnectMessage() {
        return this.connectMessage;
    }

    public void setConnectMessage(@NotNull String connectMessage) {
        this.connectMessage = connectMessage;
    }
}