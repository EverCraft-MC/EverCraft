package io.github.evercraftmc.core.api.server;

import org.jetbrains.annotations.NotNull;

public interface ECProxyServer extends ECServer {
    public @NotNull String getAllMinecraftVersions();

    public @NotNull String getDefaultServer();

    public @NotNull String getFallbackServer();

    public boolean getServer(@NotNull String name);
}