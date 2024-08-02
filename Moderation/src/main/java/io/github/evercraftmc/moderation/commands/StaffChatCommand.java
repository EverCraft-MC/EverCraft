package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StaffChatCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public StaffChatCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "staffChat";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("sc");
    }

    @Override
    public @NotNull String getDescription() {
        return "Message the staffchat";
    }

    @Override
    public @NotNull String getUsage() {
        return "/commandSpy [(on|off|<message>)]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.staffChat";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() == 0) {
            parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).staffChat = !parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).staffChat;
            parent.getPlugin().saveData();
        } else {
            if (args.get(0).equalsIgnoreCase("on") || args.get(0).equalsIgnoreCase("true")) {
                parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).staffChat = true;
                parent.getPlugin().saveData();
            } else if (args.get(0).equalsIgnoreCase("off") || args.get(0).equalsIgnoreCase("false")) {
                parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).staffChat = false;
                parent.getPlugin().saveData();
            } else {
                if (sendFeedback) {
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < args.size(); i++) {
                        message.append(args.get(i)).append(" ");
                    }

                    List<ECPlayer> recipients = new ArrayList<>();
                    for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                        if (player2.hasPermission(this.getPermission())) {
                            recipients.add(player2);
                        }
                    }
                    PlayerChatEvent newEvent = new PlayerChatEvent(player, "&d&l[Staffchat] &r" + player.getDisplayName() + " &r> " + ECTextFormatter.stripColors(message.toString().trim()), PlayerChatEvent.MessageType.STAFFCHAT, recipients);
                    parent.getPlugin().getServer().getEventManager().emit(newEvent);

                    if (newEvent.isCancelled()) {
                        if (!newEvent.getCancelReason().isEmpty()) {
                            player.sendMessage(newEvent.getCancelReason());
                        }

                        return false;
                    } else if (!newEvent.getMessage().isEmpty()) {
                        for (ECPlayer player2 : newEvent.getRecipients()) {
                            player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                        }
                    }
                }

                return true;
            }
        }

        if (sendFeedback) {
            if (parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).staffChat) {
                player.sendMessage(ECTextFormatter.translateColors("&aTurned staffchat on"));
            } else {
                player.sendMessage(ECTextFormatter.translateColors("&aTurned staffchat off"));
            }
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            return List.of("on", "off");
        } else {
            return List.of();
        }
    }
}