package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import io.github.evercraftmc.global.events.player.PlayerDisplayNameChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PrefixCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public PrefixCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "prefix";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of("setPrefix");
    }

    @Override
    public @NotNull String getDescription() {
        return "Change your prefix";
    }

    @Override
    public @NotNull String getUsage() {
        return "/prefix <prefix>";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        if (player.hasPermission("evercraft.global.commands.prefix.other")) {
            return "/prefix [<user>] <prefix>";
        } else {
            return this.getUsage();
        }
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.global.commands.prefix";
    }

    @Override
    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission(), "evercraft.global.commands.prefix.other");
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 0) {
            ECPlayer otherPlayer = parent.getPlugin().getServer().getOnlinePlayer(args.get(0));

            if (otherPlayer != null && player.hasPermission("evercraft.global.commands.prefix.other")) {
                if (args.size() == 1) {
                    this.parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).displayName = otherPlayer.getName();
                    this.parent.getPlugin().saveData();

                    if (sendFeedback) {
                        player.sendMessage(ECTextFormatter.translateColors("&a" + otherPlayer.getDisplayName() + "&r&a's prefix has been reset."));
                    }

                    otherPlayer.setOnlineDisplayName(ECTextFormatter.translateColors((parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).prefix != null ? parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).prefix + "&r " : "&r") + parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).displayName + "&r"));

                    PlayerDisplayNameChangeEvent newEvent = new PlayerDisplayNameChangeEvent(player);
                    parent.getPlugin().getServer().getEventManager().emit(newEvent);

                    return true;
                } else if (args.size() == 2) {
                    if (ECTextFormatter.stripColors(args.get(1)).length() <= 16 && args.get(1).length() <= 32) {
                        if (args.get(1).equalsIgnoreCase("reset")) {
                            this.parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).displayName = otherPlayer.getName();
                            this.parent.getPlugin().saveData();

                            if (sendFeedback) {
                                player.sendMessage(ECTextFormatter.translateColors("&a" + otherPlayer.getDisplayName() + "&r&a's prefix has been reset."));
                            }
                        } else {
                            this.parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).displayName = args.get(1);
                            this.parent.getPlugin().saveData();

                            if (sendFeedback) {
                                player.sendMessage(ECTextFormatter.translateColors("&aSuccessfully set " + otherPlayer.getDisplayName() + "&r&a's prefix to &r" + args.get(1) + "&r&a."));
                            }
                        }

                        otherPlayer.setOnlineDisplayName(ECTextFormatter.translateColors((parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).prefix != null ? parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).prefix + "&r " : "&r") + parent.getPlugin().getPlayerData().players.get(otherPlayer.getUuid().toString()).displayName + "&r"));

                        PlayerDisplayNameChangeEvent newEvent = new PlayerDisplayNameChangeEvent(player);
                        parent.getPlugin().getServer().getEventManager().emit(newEvent);

                        return true;
                    } else if (sendFeedback) {
                        player.sendMessage(ECTextFormatter.translateColors("&cThat prefix is too long."));
                    }
                } else if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cYour prefix can't contain spaces."));
                }
            } else {
                if (!(player instanceof ECConsole)) {
                    if (args.size() == 1) {
                        if (ECTextFormatter.stripColors(args.get(0)).length() <= 16 && args.get(0).length() <= 32) {
                            if (args.get(0).equalsIgnoreCase("reset")) {
                                this.parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).displayName = player.getName();
                                this.parent.getPlugin().saveData();

                                if (sendFeedback) {
                                    player.sendMessage(ECTextFormatter.translateColors("&aYour prefix has been reset."));
                                }
                            } else {
                                this.parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).displayName = args.get(0);
                                this.parent.getPlugin().saveData();

                                if (sendFeedback) {
                                    player.sendMessage(ECTextFormatter.translateColors("&aSuccessfully set your prefix to &r" + args.get(0) + "&r&a."));
                                }
                            }

                            player.setOnlineDisplayName(ECTextFormatter.translateColors((parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).prefix != null ? parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).prefix + "&r " : "&r") + parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).displayName + "&r"));

                            PlayerDisplayNameChangeEvent newEvent = new PlayerDisplayNameChangeEvent(player);
                            parent.getPlugin().getServer().getEventManager().emit(newEvent);

                            return true;
                        } else if (sendFeedback) {
                            player.sendMessage(ECTextFormatter.translateColors("&cThat prefix is too long."));
                        }
                    } else if (sendFeedback) {
                        player.sendMessage(ECTextFormatter.translateColors("&cYour prefix can't contain spaces."));
                    }
                } else if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&cYou can't do that from the console."));
                }
            }
        } else {
            if (!(player instanceof ECConsole)) {
                this.parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).displayName = player.getName();
                this.parent.getPlugin().saveData();

                if (sendFeedback) {
                    player.sendMessage(ECTextFormatter.translateColors("&aYour prefix has been reset."));
                }

                player.setOnlineDisplayName(ECTextFormatter.translateColors((parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).prefix != null ? parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).prefix + "&r " : "&r") + parent.getPlugin().getPlayerData().players.get(player.getUuid().toString()).displayName + "&r"));

                PlayerDisplayNameChangeEvent newEvent = new PlayerDisplayNameChangeEvent(player);
                parent.getPlugin().getServer().getEventManager().emit(newEvent);

                return true;
            } else if (sendFeedback) {
                player.sendMessage(ECTextFormatter.translateColors("&cYou can't do that from the console."));
            }
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            if (player.hasPermission("evercraft.global.commands.prefix.other")) {
                List<String> players = new ArrayList<>();
                players.add("reset");
                for (ECPlayer player2 : parent.getPlugin().getServer().getOnlinePlayers()) {
                    players.add(player2.getName());
                }
                return players;
            }

            return List.of("reset");
        } else if (args.size() == 2 && player.hasPermission("evercraft.global.commands.prefix.other")) {
            return List.of("reset");
        } else {
            return List.of();
        }
    }
}