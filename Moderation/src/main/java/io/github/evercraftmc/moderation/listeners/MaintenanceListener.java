package io.github.evercraftmc.moderation.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerJoinEvent;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import org.jetbrains.annotations.NotNull;

public class MaintenanceListener implements ECListener {
    protected final @NotNull ModerationModule parent;

    public MaintenanceListener(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public ModerationModule getModule() {
        return parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (parent.getPlugin().getEnvironment().getType() != ECEnvironmentType.PROXY) {
            return;
        }

        if (parent.getPlugin().getPlayerData().maintenance && !event.getPlayer().hasPermission("evercraft.moderation.commands.maintenance.bypass")) {
            event.setCancelled(true);
            event.setCancelReason(ECTextFormatter.translateColors("&cThe server is currently in maintenance mode\nCheck back later!"));
        }
    }
}