package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class LockChatCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public LockChatCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "lockChat";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of();
    }

    @Override
    public @NotNull String getDescription() {
        return "Lock/unlock the chat";
    }

    @Override
    public @NotNull String getUsage() {
        return "/lockChat [(on|off)]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public String getPermission() {
        return "evercraft.moderation.commands.lockChat";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission(), "evercraft.moderation.commands.lockChat.bypass");
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() == 0) {
            parent.getPlugin().getPlayerData().chatLocked = !parent.getPlugin().getPlayerData().chatLocked;
            parent.getPlugin().saveData();
        } else {
            if (args.get(0).equalsIgnoreCase("on") || args.get(0).equalsIgnoreCase("true")) {
                parent.getPlugin().getPlayerData().chatLocked = true;
                parent.getPlugin().saveData();
            } else if (args.get(0).equalsIgnoreCase("off") || args.get(0).equalsIgnoreCase("false")) {
                parent.getPlugin().getPlayerData().chatLocked = false;
                parent.getPlugin().saveData();
            } else {
                if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cWas expecting \"on\" or \"off\""));
                }

                return false;
            }
        }

        if (sendFeedback) {
            if (parent.getPlugin().getPlayerData().chatLocked) {
                if (player instanceof ECConsole) {
                    player.sendMessage(ECTextFormatter.translateColors("&aThe chat has been locked"));
                }
                parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&aThe chat has been locked"));
            } else {
                if (player instanceof ECConsole) {
                    player.sendMessage(ECTextFormatter.translateColors("&aThe chat has been unlocked"));
                }
                parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&aThe chat has been unlocked"));
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