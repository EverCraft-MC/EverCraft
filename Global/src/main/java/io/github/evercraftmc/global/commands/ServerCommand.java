package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ServerCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public ServerCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "server";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("goto");
    }

    @Override
    public @NotNull String getDescription() {
        return "Switch to another server";
    }

    @Override
    public @NotNull String getUsage() {
        return "/server <server>";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        if (player.hasPermission("evercraft.global.commands.server.other")) {
            return "/server [<user>] <server>";
        } else {
            return this.getUsage();
        }
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.global.commands.server";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission(), "evercraft.global.commands.server.other");
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 0) {
            ECPlayer otherPlayer = parent.getPlugin().getServer().getOnlinePlayer(args.get(0));

            if (otherPlayer != null && player.hasPermission("evercraft.global.commands.nickname.other")) {
                if (args.size() == 2) {
                    ECProxyServer.Server server = ((ECProxyServer) parent.getPlugin().getServer()).getServer(args.get(0));

                    if (server != null) {
                        ((ECProxyPlayer) otherPlayer).setServer(server);

                        return true;
                    } else {
                        player.sendMessage(ECTextFormatter.translateColors("&cUnknown server \"" + args.get(0) + "\"!"));
                    }
                } else if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cToo many arguments!"));
                }
            } else {
                if (!(player instanceof ECConsole)) {
                    if (args.size() == 1) {
                        ECProxyServer.Server server = ((ECProxyServer) parent.getPlugin().getServer()).getServer(args.get(0));

                        if (server != null) {
                            ((ECProxyPlayer) player).setServer(server);

                            return true;
                        } else {
                            player.sendMessage(ECTextFormatter.translateColors("&cUnknown server \"" + args.get(0) + "\"!"));
                        }
                    } else if (sendFeedback) {
                        player.sendMessage(ECTextFormatter.translateColors("&cToo many arguments!"));
                    }
                } else if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cYou can't do that from the console."));
                }
            }

            return false;
        } else if (sendFeedback) {
            player.sendMessage(ECTextFormatter.translateColors("&cYou must pass a server."));
            return false;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            List<String> servers = new ArrayList<>();
            for (ECProxyServer.Server server : ((ECProxyServer) parent.getPlugin().getServer()).getServers()) {
                servers.add(server.name());
            }

            if (player.hasPermission("evercraft.global.commands.server.other")) {
                for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                    servers.add(player2.getName());
                }
            }

            return servers;
        } else if (args.size() == 2 && player.hasPermission("evercraft.global.commands.server.other")) {
            List<String> servers = new ArrayList<>();
            for (ECProxyServer.Server server : ((ECProxyServer) parent.getPlugin().getServer()).getServers()) {
                servers.add(server.name());
            }
            return servers;
        } else {
            return List.of();
        }
    }
}