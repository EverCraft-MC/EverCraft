package io.github.evercraftmc.global.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerProxyPingEvent;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ServerPingListener implements ECListener {
    protected final @NotNull GlobalModule parent;

    public ServerPingListener(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerPing(@NotNull PlayerProxyPingEvent event) {
        String server = ((ECProxyServer) parent.getPlugin().getServer()).getDefaultServer();
        boolean isDefault = true;

        if (event.getServerAddress() != null) {
            String[] address = event.getServerAddress().getHostString().split("\\.");
            if (address.length > 2 && !address[0].equalsIgnoreCase(((ECProxyServer) parent.getPlugin().getServer()).getFallbackServer()) && ((ECProxyServer) parent.getPlugin().getServer()).getServer(address[0].toLowerCase()) != null) {
                server = address[0].toLowerCase();
                isDefault = false;
            }
        }

        String[] motd = new String[2];
        motd[0] = ECTextFormatter.translateColors("&r&6--------[ &3&lEverCraft &r&7" + ((ECProxyServer) parent.getPlugin().getServer()).getAllMinecraftVersions() + " &r&6]--------");
        if (isDefault) {
            motd[1] = ECTextFormatter.translateColors("&7Lots of fun minigames!");
        } else {
            motd[1] = ECTextFormatter.translateColors("&r" + server.substring(0, 1).toUpperCase() + server.substring(1).toLowerCase());
        }
        event.setMotd(String.join("\n", motd));
        event.setCenterMotd(true);

        event.setOnlinePlayers(this.parent.getPlugin().getServer().getOnlinePlayers().size());
        event.setMaxPlayers(100);

        Map<UUID, String> onlinePlayers = new HashMap<>();
        for (ECPlayer player : this.parent.getPlugin().getServer().getOnlinePlayers()) {
            onlinePlayers.put(player.getUuid(), player.getDisplayName());
        }
        event.setPlayers(onlinePlayers);
    }
}