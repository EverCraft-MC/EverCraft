package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ReplyCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public ReplyCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "reply";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("r");
    }

    @Override
    public @NotNull String getDescription() {
        return "Reply to the last player you messaged";
    }

    @Override
    public @NotNull String getUsage() {
        return "/reply <message>";
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
            ECPlayer player2 = parent.getPlugin().getServer().getOnlinePlayer(MessageCommand.lastMessaged.get(player));

            if (player2 != null) {
                List<String> args2 = new ArrayList<>();
                args2.add(player2.getName());
                args2.addAll(args);

                return Objects.requireNonNull(parent.getPlugin().getServer().getCommandManager().get("message")).run(player, args2, sendFeedback);
            } else {
                player.sendMessage(ECTextFormatter.translateColors("&cYou have not messaged anyone."));
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
        return List.of();
    }
}