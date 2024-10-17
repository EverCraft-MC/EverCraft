package io.github.evercraftmc.core.api.server.player;

import io.github.evercraftmc.core.api.server.ECServerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECProxyPlayer extends ECPlayer {
    public @Nullable ECServerInfo getServer();

    public void setServer(@NotNull ECServerInfo server);
}