package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public HelpCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("?");
    }

    @Override
    public @NotNull String getDescription() {
        return "View the help info";
    }

    @Override
    public @NotNull String getUsage() {
        return "/help [<command>]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.global.commands.help";
    }

    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 0) {
            ECCommand command = parent.getPlugin().getServer().getCommandManager().get(args.get(0));
            if (command == null || !(command.getPermission() == null || player.hasPermission(command.getPermission()))) {
                player.sendMessage(ECTextFormatter.translateColors("&cCommand \"" + args.get(0) + "\" could not be found"));
                return false;
            }

            StringBuilder out = new StringBuilder();

            out.append("&r&l&a").append(command.getName());
            if (!command.getAlias().isEmpty()) {
                out.append(" ").append("(").append(String.join(", ", command.getAlias())).append(")");
            }
            out.append("\n");
            out.append("&r&a").append(command.getUsage(player)).append("\n\n");
            out.append("&r&a").append(command.getDescription());

            player.sendMessage(ECTextFormatter.translateColors(out.toString().trim()));
        } else {
            StringBuilder out = new StringBuilder();

            for (ECCommand command : parent.getPlugin().getServer().getCommandManager().getAll()) {
                if (command.getPermission() == null || player.hasPermission(command.getPermission())) {
                    out.append("&r&a").append(command.getUsage(player)).append(" - ").append(command.getDescription()).append("\n");
                }
            }

            player.sendMessage(ECTextFormatter.translateColors(out.toString().trim()));
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            return List.copyOf(parent.getPlugin().getServer().getCommandManager().getAll().stream().map(ECCommand::getName).toList());
        } else {
            return List.of();
        }
    }
}