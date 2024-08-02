package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import io.github.evercraftmc.moderation.util.TimeUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MuteCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public MuteCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "mute";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of();
    }

    @Override
    public @NotNull String getDescription() {
        return "Mute a player";
    }

    @Override
    public @NotNull String getUsage() {
        return "/mute <player> <time> [<reason>]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.mute";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 0) {
            ECPlayer player2 = parent.getPlugin().getServer().getPlayer(args.get(0));

            if (player2 != null) {
                Instant until = TimeUtil.parseFuture(args.get(1));

                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 2; i < args.size(); i++) {
                    reasonBuilder.append(args.get(i)).append(" ");
                }
                String reason = reasonBuilder.toString().trim();

                if (sendFeedback) {
                    if (!reason.isEmpty()) {
                        parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&r" + player2.getDisplayName() + " &r&ahas been muted by &r" + player.getDisplayName() + " &r&afor \"" + TimeUtil.stringifyFuture(until, true) + "\" because \"&r" + reason + "&r&a\"."));

                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute = new ECPlayerData.Player.Moderation();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.moderator = player.getUuid();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.reason = reason;
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.date = Instant.now();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.until = until;
                        parent.getPlugin().saveData();

                        ECPlayer onlinePlayer2 = parent.getPlugin().getServer().getOnlinePlayer(player2.getUuid());
                        if (onlinePlayer2 != null) {
                            onlinePlayer2.sendMessage(ECTextFormatter.translateColors("&cYou have been muted by &r" + player.getDisplayName() + " &r&cfor \"" + TimeUtil.stringifyFuture(until, true) + "\" because \"&r" + reason + "&r&c\"."));
                        }

                        player.sendMessage(ECTextFormatter.translateColors("&aSuccessfully muted player &r" + player.getDisplayName() + " &r&afor \"" + TimeUtil.stringifyFuture(until, true) + "\" because \"&r" + reason + "&r&a\"."));
                    } else {
                        parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&r" + player2.getDisplayName() + " &r&ahas been muted by &r" + player.getDisplayName() + " &r&afor \"" + TimeUtil.stringifyFuture(until, true) + "\"."));

                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute = new ECPlayerData.Player.Moderation();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.moderator = player.getUuid();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.reason = reason;
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.date = Instant.now();
                        parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).mute.until = until;
                        parent.getPlugin().saveData();

                        ECPlayer onlinePlayer2 = parent.getPlugin().getServer().getOnlinePlayer(player2.getUuid());
                        if (onlinePlayer2 != null) {
                            onlinePlayer2.sendMessage(ECTextFormatter.translateColors("&cYou have been muted by &r" + player.getDisplayName() + " &r&cfor \"" + TimeUtil.stringifyFuture(until, true) + "\"."));
                        }

                        player.sendMessage(ECTextFormatter.translateColors("&aSuccessfully muted player &r" + player2.getDisplayName() + " &r&afor \"" + TimeUtil.stringifyFuture(until, true) + "\"."));
                    }
                }

                return true;
            } else if (sendFeedback) {
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
        } else if (args.size() == 2) {
            return List.of("forever", "5m", "30m", "1h", "6h", "1d", "3d", "1w");
        } else {
            return List.of();
        }
    }
}