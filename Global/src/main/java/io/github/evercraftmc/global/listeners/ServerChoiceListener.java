package io.github.evercraftmc.global.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerProxyJoinEvent;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerServerConnectedEvent;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import org.jetbrains.annotations.NotNull;

public class ServerChoiceListener implements ECListener {
    protected final @NotNull GlobalModule parent;

    public ServerChoiceListener(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerJoin(@NotNull PlayerProxyJoinEvent event) {
        String server = ((ECProxyServer) parent.getPlugin().getServer()).getDefaultServer().name();

        if (event.getPlayer().getServerAddress() != null) {
            String[] address = event.getPlayer().getServerAddress().getHostString().split("\\.");
            if (address.length > 2 && !address[0].equalsIgnoreCase(((ECProxyServer) parent.getPlugin().getServer()).getFallbackServer().name()) && ((ECProxyServer) parent.getPlugin().getServer()).getServer(address[0].toLowerCase()) != null) {
                server = address[0].toLowerCase();
            }
        }

        event.setTargetServer(server);
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerConnect(@NotNull PlayerServerConnectedEvent event) {
        event.setConnectMessage(ECTextFormatter.translateColors("&e" + event.getPlayer().getDisplayName() + " &r&emoved to " + event.getTargetServer()));
    }
}