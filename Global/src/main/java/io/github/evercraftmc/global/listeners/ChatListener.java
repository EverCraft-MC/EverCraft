package io.github.evercraftmc.global.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.messaging.ECEnvironmentTypeMessageId;
import io.github.evercraftmc.core.messaging.ECMessageType;
import io.github.evercraftmc.global.GlobalModule;
import io.github.evercraftmc.messaging.common.ECMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements ECListener {
    protected final @NotNull GlobalModule parent;

    public ChatListener(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        if (parent.getPlugin().getEnvironment().getType() == ECEnvironmentType.PROXY) {
            if (event.getType() == PlayerChatEvent.MessageType.CHAT) {
                event.setMessage(ECTextFormatter.translateColors("&r" + event.getPlayer().getDisplayName() + " &r> ") + ECTextFormatter.stripColors(event.getMessage()));
            } else if (event.getType() == PlayerChatEvent.MessageType.DEATH || event.getType() == PlayerChatEvent.MessageType.ADVANCEMENT) {
                String message = event.getMessage();
                for (ECPlayer player : parent.getPlugin().getServer().getOnlinePlayers()) {
                    // FIXME Impartial matches. Kale matches Kale_Ko
                    message = message.replace(player.getName(), player.getDisplayName());
                }

                event.setMessage(ECTextFormatter.translateColors("&r" + message));
            }
        } else {
            event.setCancelled(true);

            if (event.getType() != PlayerChatEvent.MessageType.CHAT) {
                try {
                    ByteArrayOutputStream chatMessageData = new ByteArrayOutputStream();
                    DataOutputStream chatMessage = new DataOutputStream(chatMessageData);
                    chatMessage.writeInt(ECMessageType.GLOBAL_CHAT);
                    chatMessage.writeUTF(event.getPlayer().getUuid().toString());
                    chatMessage.writeInt(event.getType());
                    chatMessage.writeUTF(event.getMessage());
                    chatMessage.writeInt(event.getRecipients().size());
                    for (ECPlayer recipient : event.getRecipients()) {
                        chatMessage.writeUTF(recipient.getUuid().toString());
                    }
                    chatMessage.close();

                    parent.getPlugin().getMessenger().send(new ECEnvironmentTypeMessageId(ECEnvironmentType.PROXY), chatMessageData.toByteArray());
                } catch (IOException e) {
                    parent.getPlugin().getLogger().error("[Messenger] Failed to send message", e);
                }
            }
        }
    }

    @ECHandler(order=ECHandlerOrder.BEFORE)
    public void onGlobalChat(@NotNull MessageEvent event) {
        ECMessage message = event.getMessage();

        if (!message.getSender().matches(parent.getPlugin().getServer()) && message.getRecipient().matches(parent.getPlugin().getServer())) {
            try {
                ByteArrayInputStream commandMessageData = new ByteArrayInputStream(message.getData());
                DataInputStream commandMessage = new DataInputStream(commandMessageData);

                int type = commandMessage.readInt();
                if (type == ECMessageType.GLOBAL_CHAT) {
                    UUID uuid = UUID.fromString(commandMessage.readUTF());
                    int chatType = commandMessage.readInt();
                    String chat = commandMessage.readUTF();
                    List<ECPlayer> recipients = new ArrayList<>();
                    int recipientCount = commandMessage.readInt();
                    for (int i = 0; i < recipientCount; i++) {
                        recipients.add(parent.getPlugin().getServer().getOnlinePlayer(UUID.fromString(commandMessage.readUTF())));
                    }

                    ECPlayer player = parent.getPlugin().getServer().getOnlinePlayer(uuid);

                    PlayerChatEvent newEvent = new PlayerChatEvent(player, chatType, chat, recipients);
                    parent.getPlugin().getServer().getEventManager().emit(newEvent);

                    if (newEvent.isCancelled()) {
                        if (!newEvent.getCancelReason().isEmpty()) {
                            player.sendMessage(newEvent.getCancelReason());
                        }
                    } else if (!newEvent.getMessage().isEmpty()) {
                        for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getPlugin().getServer().getOnlinePlayers())) {
                            if (player instanceof ECProxyPlayer proxyPlayer && (proxyPlayer.getServer() == null || !(player2 instanceof ECProxyPlayer proxyPlayer2 && proxyPlayer.getServer().equals(proxyPlayer2.getServer())))) {
                                player2.sendMessage(ECTextFormatter.translateColors("&r[" + proxyPlayer.getServer().name().substring(0, 1).toUpperCase() + proxyPlayer.getServer().name().substring(1).toLowerCase() + "&r] " + newEvent.getMessage()));
                            } else {
                                player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                            }
                        }
                    }
                }

                commandMessage.close();
            } catch (IOException e) {
                parent.getPlugin().getLogger().error("[Messenger] Failed to read message", e);
            }
        }
    }
}