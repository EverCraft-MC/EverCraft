package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public class MessageCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public static final @NotNull Map<ECPlayer, UUID> lastMessaged = new HashMap<>();

    public MessageCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "message";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("msg", "whisper", "w");
    }

    @Override
    public @NotNull String getDescription() {
        return "Message another player";
    }

    @Override
    public @NotNull String getUsage() {
        return "/message <player> <message>";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.global.commands.message";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 0) {
            ECPlayer player2 = parent.getPlugin().getServer().getOnlinePlayer(args.get(0));

            if (player2 != null) {
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 1; i < args.size(); i++) {
                    messageBuilder.append(args.get(i)).append(" ");
                }
                String message = messageBuilder.toString().trim();

                if (sendFeedback) {
                    List<ECPlayer> recipients = List.of(player2);
                    PlayerChatEvent newEvent = new PlayerChatEvent(player, "&b&l[DM] &r" + player.getDisplayName() + " &r&8-> &4You &r> " + ECTextFormatter.stripColors(message.trim()), PlayerChatEvent.MessageType.DM, recipients);
                    parent.getPlugin().getServer().getEventManager().emit(newEvent);

                    if (newEvent.isCancelled()) {
                        if (!newEvent.getCancelReason().isEmpty()) {
                            player.sendMessage(newEvent.getCancelReason());
                        }
                        return false;
                    } else if (!newEvent.getMessage().isEmpty()) {
                        lastMessaged.remove(player);
                        lastMessaged.put(player, player2.getUuid());
                        lastMessaged.remove(player2);
                        lastMessaged.put(player2, player.getUuid());

                        player2.sendMessage(ECTextFormatter.translateColors("&b&l[DM] &r" + player.getDisplayName() + " &r&8-> &4You &r> " + ECTextFormatter.stripColors(message.trim())));
                        player.sendMessage(ECTextFormatter.translateColors("&b&l[DM] &r&4You &r&8-> &r" + player.getDisplayName() + " &r> " + ECTextFormatter.stripColors(message.trim())));
                        return true;
                    }
                }
            } else {
                player.sendMessage(ECTextFormatter.translateColors("&cPlayer \"" + args.get(0) + "\" could not be found."));
                return false;
            }
        } else if (sendFeedback) {
            player.sendMessage(ECTextFormatter.translateColors("&cYou must pass a username."));
            return false;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            List<String> players = new ArrayList<>();
            for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                players.add(player2.getName());
            }
            return players;
        } else {
            return List.of();
        }
    }
}