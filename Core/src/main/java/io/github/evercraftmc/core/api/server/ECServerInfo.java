package io.github.evercraftmc.core.api.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import java.net.InetSocketAddress;
import org.jetbrains.annotations.NotNull;

public record ECServerInfo(@NotNull String name, @NotNull InetSocketAddress address) {
    public static @NotNull ECServerInfo from(@NotNull ServerInfo serverInfo) {
        return new ECServerInfo(serverInfo.getName(), serverInfo.getAddress());
    }
}