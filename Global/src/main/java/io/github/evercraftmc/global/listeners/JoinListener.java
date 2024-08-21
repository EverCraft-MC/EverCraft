package io.github.evercraftmc.global.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerJoinEvent;
import io.github.evercraftmc.core.api.events.player.PlayerLeaveEvent;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements ECListener {
    protected final @NotNull GlobalModule parent;

    public JoinListener(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public GlobalModule getModule() {
        return parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (parent.getPlugin().getEnvironment().getType() == ECEnvironmentType.PROXY) {
            if (parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).firstJoin == null) {
                parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).firstJoin = Instant.now();

                parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&b&lWelcome " + event.getPlayer().getDisplayName() + " &r&bto the server!"));
            }

            parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).lastJoin = Instant.now();

            parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).lastIp = event.getPlayer().getAddress();

            parent.getPlugin().saveData();
        }

        if (parent.getPlugin().getEnvironment().getType() == ECEnvironmentType.PROXY) {
            event.setJoinMessage(ECTextFormatter.translateColors("&e" + event.getPlayer().getDisplayName() + " &r&ejoined the server!"));
        } else {
            event.setJoinMessage(ECTextFormatter.translateColors(""));
        }
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerLeave(@NotNull PlayerLeaveEvent event) {
        if (parent.getPlugin().getEnvironment().getType() == ECEnvironmentType.PROXY) {
            parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).lastJoin = Instant.now();
        }

        if (parent.getPlugin().getEnvironment().getType() == ECEnvironmentType.PROXY) {
            event.setLeaveMessage(ECTextFormatter.translateColors("&e" + event.getPlayer().getDisplayName() + " &r&eleft the server"));
        } else {
            event.setLeaveMessage(ECTextFormatter.translateColors(""));
        }
    }
}