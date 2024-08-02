package io.github.evercraftmc.core.api.server.player;

import io.github.evercraftmc.core.api.server.ECProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECProxyPlayer extends ECPlayer {
    public @Nullable ECProxyServer.Server getServer();

    public void setServer(@NotNull ECProxyServer.Server server);
}