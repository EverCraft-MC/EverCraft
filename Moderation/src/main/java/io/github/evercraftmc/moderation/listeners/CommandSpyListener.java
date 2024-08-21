package io.github.evercraftmc.moderation.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerCommandEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import org.jetbrains.annotations.NotNull;

public class CommandSpyListener implements ECListener {
    protected final @NotNull ModerationModule parent;

    public CommandSpyListener(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public ModerationModule getModule() {
        return parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerChat(@NotNull PlayerCommandEvent event) {
        if (parent.getPlugin().getEnvironment().getType() != ECEnvironmentType.PROXY) {
            return;
        }

        if (!event.getPlayer().hasPermission("evercraft.moderation.commands.commandSpy.extra") && (event.getCommand().toLowerCase().startsWith("/message") || event.getCommand().toLowerCase().startsWith("/msg") || event.getCommand().toLowerCase().startsWith("/reply") || event.getCommand().toLowerCase().startsWith("/r") || event.getCommand().toLowerCase().startsWith("/staffchat") || event.getCommand().toLowerCase().startsWith("/sc"))) {
            return;
        }

        for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
            if (!event.getPlayer().getName().equals(player2.getName()) && parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).commandSpy && player2.hasPermission("evercraft.moderation.commands.commandSpy")) {
                player2.sendMessage(ECTextFormatter.translateColors("&d&l[CommandSpy] &r" + event.getPlayer().getDisplayName() + " &r> ") + event.getCommand().trim());
            }
        }
    }
}