package io.github.evercraftmc.core.api.events.proxy.player;

import io.github.evercraftmc.core.api.events.player.PlayerJoinEvent;
import io.github.evercraftmc.core.api.server.ECServerInfo;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerProxyJoinEvent extends PlayerJoinEvent {
    protected @NotNull ECServerInfo targetServer;

    public PlayerProxyJoinEvent(@NotNull ECPlayer player, @NotNull String joinMessage, @NotNull ECServerInfo targetServer) {
        super(player, joinMessage);

        this.targetServer = targetServer;
    }

    public @NotNull ECServerInfo getTargetServer() {
        return this.targetServer;
    }

    public void setTargetServer(@NotNull ECServerInfo targetServer) {
        this.targetServer = targetServer;
    }
}