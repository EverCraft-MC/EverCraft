package io.github.evercraftmc.moderation.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MaintenanceCommand implements ECCommand {
    protected final @NotNull ModerationModule parent;

    public MaintenanceCommand(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public ModerationModule getModule() {
        return parent;
    }

    @Override
    public @NotNull String getName() {
        return "maintenance";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of();
    }

    @Override
    public @NotNull String getDescription() {
        return "Turn maintenance on/off";
    }

    @Override
    public @NotNull String getUsage() {
        return "/maintenance [(on|off)]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.moderation.commands.maintenance";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission(), "evercraft.moderation.commands.maintenance.bypass");
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() == 0) {
            parent.getPlugin().getPlayerData().maintenance = !parent.getPlugin().getPlayerData().maintenance;
            parent.getPlugin().saveData();
        } else {
            if (args.get(0).equalsIgnoreCase("on") || args.get(0).equalsIgnoreCase("true")) {
                parent.getPlugin().getPlayerData().maintenance = true;
                parent.getPlugin().saveData();
            } else if (args.get(0).equalsIgnoreCase("off") || args.get(0).equalsIgnoreCase("false")) {
                parent.getPlugin().getPlayerData().maintenance = false;
                parent.getPlugin().saveData();
            } else {
                if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cWas expecting \"on\" or \"off\""));
                }

                return false;
            }
        }

        if (sendFeedback) {
            if (parent.getPlugin().getPlayerData().maintenance) {
                if (player instanceof ECConsole) {
                    player.sendMessage(ECTextFormatter.translateColors("&aMaintenance mode has been enabled"));
                }
                parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&aMaintenance mode has been enabled"));

                for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                    if (!player2.hasPermission("evercraft.moderation.commands.maintenance.bypass")) {
                        player2.kick(ECTextFormatter.translateColors("&cThe server is currently in maintenance mode\nCheck back later!"));
                    }
                }
            } else {
                if (player instanceof ECConsole) {
                    player.sendMessage(ECTextFormatter.translateColors("&aMaintenance mode has been disabled"));
                }
                parent.getPlugin().getServer().broadcastMessage(ECTextFormatter.translateColors("&aMaintenance mode has been disabled"));
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