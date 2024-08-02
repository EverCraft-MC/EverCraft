package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UnbanCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public UnbanCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "unban";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of();
    }

    @Override
    public @NotNull String getDescription() {
        return "Unban a player";
    }

    @Override
    public @NotNull String getUsage() {
        return "/unban <player>";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.ban";
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
                if (sendFeedback) {
                    parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&r" + player2.getDisplayName() + " &r&ahas been unbanned by &r" + player.getDisplayName() + "&r&a."));

                    parent.getPlugin().getPlayerData().players.get(player2.getUuid().toString()).ban = null;
                    parent.getPlugin().saveData();

                    ECPlayer onlinePlayer2 = parent.getPlugin().getServer().getOnlinePlayer(player2.getUuid());
                    if (onlinePlayer2 != null) {
                        onlinePlayer2.sendMessage(ECTextFormatter.translateColors("&aYou have been unbanned by &r" + player.getDisplayName() + "&r&a."));
                    }

                    player.sendMessage(ECTextFormatter.translateColors("&aSuccessfully unbanned player &r" + player2.getDisplayName() + "&r&a."));
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
        } else {
            return List.of();
        }
    }
}