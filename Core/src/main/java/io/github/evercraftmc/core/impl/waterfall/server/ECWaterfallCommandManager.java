package io.github.evercraftmc.core.impl.waterfall.server;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.core.api.server.ECCommandManager;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.waterfall.util.ECWaterfallComponentFormatter;
import io.github.evercraftmc.core.messaging.ECMessage;
import io.github.evercraftmc.core.messaging.ECMessageType;
import io.github.evercraftmc.core.messaging.ECRecipient;
import java.io.*;
import java.util.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECWaterfallCommandManager implements ECCommandManager {
    protected class CommandInter extends Command implements TabExecutor {
        protected final @NotNull ECWaterfallCommandManager parent = ECWaterfallCommandManager.this;

        protected final @NotNull ECCommand command;
        protected final boolean forwardToOther;

        public CommandInter(@NotNull ECCommand command, boolean distinguishServer, boolean forwardToOther) {
            super((distinguishServer ? "b" : "") + command.getName().toLowerCase(), command.getPermission() != null ? command.getPermission().toLowerCase() : null, CommandInter.alias(command.getName(), command.getAlias(), distinguishServer).toArray(new String[] { }));

            this.command = command;
            this.forwardToOther = forwardToOther;
        }

        @Override
        public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
            if (sender instanceof ProxiedPlayer bungeePlayer) {
                if (this.getPermission() == null || sender.hasPermission(this.getPermission())) {
                    this.command.run(parent.server.getOnlinePlayer(bungeePlayer.getUniqueId()), Arrays.asList(args), true);

                    if (this.forwardToOther) {
                        try {
                            ByteArrayOutputStream commandMessageData = new ByteArrayOutputStream();
                            DataOutputStream commandMessage = new DataOutputStream(commandMessageData);
                            commandMessage.writeInt(ECMessageType.GLOBAL_COMMAND);
                            commandMessage.writeUTF(bungeePlayer.getUniqueId().toString());
                            commandMessage.writeUTF(this.getName());
                            commandMessage.writeInt(args.length);
                            for (String arg : args) {
                                commandMessage.writeUTF(arg);
                            }
                            commandMessage.close();

                            parent.server.getPlugin().getMessenger().send(ECRecipient.fromEnvironmentType(ECEnvironmentType.BACKEND), commandMessageData.toByteArray());
                        } catch (IOException e) {
                            parent.server.getPlugin().getLogger().error("[Messenger] Failed to send message", e);
                        }
                    }
                } else {
                    sender.sendMessage(ECWaterfallComponentFormatter.stringToComponent(ECTextFormatter.translateColors("&cYou do not have permission to run that command")));
                }
            } else {
                this.command.run(parent.server.getConsole(), Arrays.asList(args), true);
            }
        }

        @Override
        public @NotNull Iterable<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
            if (sender instanceof ProxiedPlayer bungeePlayer) {
                if (this.getPermission() == null || sender.hasPermission(this.getPermission())) {
                    return this.command.tabComplete(parent.server.getOnlinePlayer(bungeePlayer.getUniqueId()), Arrays.asList(args));
                } else {
                    return List.of();
                }
            } else {
                return this.command.tabComplete(parent.server.getConsole(), Arrays.asList(args));
            }
        }

        private static @NotNull List<String> alias(@NotNull String uName, @NotNull List<String> uAliases, boolean distinguishServer) {
            ArrayList<String> aliases = new ArrayList<>();

            aliases.add("evercraft:" + (distinguishServer ? "b" : "") + uName.toLowerCase());

            for (String alias : uAliases) {
                aliases.add((distinguishServer ? "b" : "") + alias.toLowerCase());
                aliases.add("evercraft:" + (distinguishServer ? "b" : "") + alias.toLowerCase());
            }

            return aliases;
        }
    }

    protected final @NotNull ECWaterfallServer server;

    protected final @NotNull Map<String, ECCommand> commands = new HashMap<>();
    protected final @NotNull Map<String, CommandInter> interCommands = new HashMap<>();

    public ECWaterfallCommandManager(@NotNull ECWaterfallServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final ECWaterfallCommandManager parent = ECWaterfallCommandManager.this;

            @ECHandler
            public void onMessage(@NotNull MessageEvent event) {
                ECMessage message = event.getMessage();

                if (!message.getSender().matches(parent.server) && message.getRecipient().matches(parent.server)) {
                    try {
                        ByteArrayInputStream commandMessageData = new ByteArrayInputStream(message.getData());
                        DataInputStream commandMessage = new DataInputStream(commandMessageData);

                        int type = commandMessage.readInt();
                        if (type == ECMessageType.GLOBAL_COMMAND) {
                            UUID uuid = UUID.fromString(commandMessage.readUTF());
                            String command = commandMessage.readUTF();
                            List<String> args = new ArrayList<>();
                            int argC = commandMessage.readInt();
                            for (int i = 0; i < argC; i++) {
                                args.add(commandMessage.readUTF());
                            }

                            ECPlayer player = parent.server.getOnlinePlayer(uuid);
                            if (player != null) {
                                ECCommand ecCommand = parent.server.getCommandManager().get(command);
                                if (ecCommand != null) {
                                    ecCommand.run(player, args, false);
                                }
                            }
                        }

                        commandMessage.close();
                    } catch (IOException e) {
                        parent.server.getPlugin().getLogger().error("[Messenger] Failed to read message", e);
                    }
                }
            }
        });
    }

    public @NotNull ECWaterfallServer getServer() {
        return this.server;
    }

    @Override
    public @NotNull List<ECCommand> getAll() {
        return new ArrayList<>(this.commands.values());
    }

    @Override
    public @Nullable ECCommand get(@NotNull String name) {
        return this.commands.get(name.toLowerCase());
    }

    @Override
    public @NotNull ECCommand register(@NotNull ECCommand command) {
        return this.register(command, false);
    }

    @Override
    public @NotNull ECCommand register(@NotNull ECCommand command, boolean distinguishServer) {
        return this.register(command, distinguishServer, !distinguishServer);
    }

    @Override
    public @NotNull ECCommand register(@NotNull ECCommand command, boolean distinguishServer, boolean forwardToOther) {
        if (!this.commands.containsKey(command.getName().toLowerCase())) {
            CommandInter interCommand = new CommandInter(command, distinguishServer, forwardToOther);

            this.commands.put(command.getName().toLowerCase(), command);
            this.interCommands.put(command.getName().toLowerCase(), interCommand);

            this.server.getHandle().getPluginManager().registerCommand((Plugin) this.server.getPlugin().getHandle(), interCommand);

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is already registered");
        }
    }

    @Override
    public @NotNull ECCommand unregister(@NotNull ECCommand command) {
        if (this.commands.containsKey(command.getName().toLowerCase())) {
            this.server.getHandle().getPluginManager().unregisterCommand(this.interCommands.get(command.getName().toLowerCase()));

            this.commands.remove(command.getName().toLowerCase());
            this.interCommands.remove(command.getName().toLowerCase());

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is not registered");
        }
    }

    @Override
    public void unregisterAll() {
        for (ECCommand command : this.commands.values()) {
            this.unregister(command);
        }
    }
}