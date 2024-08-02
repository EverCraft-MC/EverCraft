package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public ClearChatCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "clearChat";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("cleanChat");
    }

    @Override
    public @NotNull String getDescription() {
        return "Clear the chat";
    }

    @Override
    public @NotNull String getUsage() {
        return "/clearChat";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.clearChat";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (sendFeedback) {
            for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                if (!player2.hasPermission(this.getPermission())) {
                    player2.sendMessage("\n".repeat(1200));
                } else {
                    player2.sendMessage(ECTextFormatter.translateColors("&aThe chat has been cleared."));
                }
            }
        }

        return true;
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