package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CommandSpyCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public CommandSpyCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "commandSpy";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("cs");
    }

    @Override
    public @NotNull String getDescription() {
        return "Toggle command spy";
    }

    @Override
    public @NotNull String getUsage() {
        return "/commandSpy [(on|off)]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.commandSpy";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() == 0) {
            parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).commandSpy = !parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).commandSpy;
            parent.getPlugin().saveData();
        } else {
            if (args.get(0).equalsIgnoreCase("on") || args.get(0).equalsIgnoreCase("true")) {
                parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).commandSpy = true;
                parent.getPlugin().saveData();
            } else if (args.get(0).equalsIgnoreCase("off") || args.get(0).equalsIgnoreCase("false")) {
                parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).commandSpy = false;
                parent.getPlugin().saveData();
            } else {
                if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cWas expecting \"on\" or \"off\""));
                }

                return false;
            }
        }

        if (sendFeedback) {
            if (parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).commandSpy) {
                player.sendMessage(ECTextFormatter.translateColors("&aTurned command spy on"));
            } else {
                player.sendMessage(ECTextFormatter.translateColors("&aTurned command spy off"));
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