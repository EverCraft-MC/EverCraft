package io.github.evercraftmc.moderation.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StaffChatListener implements ECListener {
    protected final @NotNull ModerationModule parent;

    public StaffChatListener(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @ECHandler(order=ECHandlerOrder.AFTER)
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        if (event.getType() == PlayerChatEvent.MessageType.CHAT && parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).staffChat && event.getPlayer().hasPermission("evercraft.moderation.commands.staffChat")) {
            event.setCancelled(true);

            List<ECPlayer> recipients = new ArrayList<>();
            for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                if (player2.hasPermission("evercraft.moderation.commands.staffchat")) {
                    recipients.add(player2);
                }
            }
            PlayerChatEvent newEvent = new PlayerChatEvent(event.getPlayer(), PlayerChatEvent.MessageType.STAFFCHAT, "&d&l[Staffchat] &r" + event.getMessage(), recipients);
            parent.getPlugin().getServer().getEventManager().emit(newEvent);

            if (newEvent.isCancelled()) {
                if (!newEvent.getCancelReason().isEmpty()) {
                    event.getPlayer().sendMessage(newEvent.getCancelReason());
                }
            } else if (!newEvent.getMessage().isEmpty()) {
                for (ECPlayer player2 : newEvent.getRecipients()) {
                    player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                }
            }
        }
    }
}