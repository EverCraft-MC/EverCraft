package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.core.api.server.ECCommandManager;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.messaging.ECMessage;
import io.github.evercraftmc.core.messaging.ECMessageType;
import io.github.evercraftmc.core.messaging.ECRecipient;
import java.io.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECVelocityCommandManager implements ECCommandManager {
    protected class CommandInter implements SimpleCommand {
        protected final @NotNull ECVelocityCommandManager parent = ECVelocityCommandManager.this;

        protected final @NotNull ECCommand command;
        protected final boolean forwardToOther;

        public CommandInter(@NotNull ECCommand command, boolean forwardToOther) {
            this.command = command;
            this.forwardToOther = forwardToOther;
        }

        @Override
        public void execute(@NotNull Invocation invocation) {
            CommandSource sender = invocation.source();
            String label = invocation.alias();
            List<String> args = new ArrayList<>(Arrays.asList(invocation.arguments()));

            if (sender instanceof Player velocityPlayer) {
                if (this.command.getPermission() == null || sender.hasPermission(this.command.getPermission())) {
                    try {
                        this.command.run(parent.server.getOnlinePlayer(velocityPlayer.getUniqueId()), args, true);
                    } catch (Exception e) {
                        parent.getServer().getPlugin().getLogger().error("Error while running command {}.", label, e);

                        return;
                    }

                    if (this.forwardToOther) {
                        try {
                            ByteArrayOutputStream commandMessageData = new ByteArrayOutputStream();
                            DataOutputStream commandMessage = new DataOutputStream(commandMessageData);
                            commandMessage.writeInt(ECMessageType.GLOBAL_COMMAND);
                            commandMessage.writeUTF(velocityPlayer.getUniqueId().toString());
                            commandMessage.writeUTF(this.command.getName());
                            commandMessage.writeInt(args.size());
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
                    sender.sendMessage(ECComponentFormatter.stringToComponent(ECTextFormatter.translateColors("&cYou do not have permission to run that command")));
                }
            } else {
                this.command.run(parent.server.getConsole(), args, true);
            }
        }

        @Override
        public List<String> suggest(@NotNull Invocation invocation) {
            CommandSource sender = invocation.source();
            String label = invocation.alias();
            List<String> args = new ArrayList<>(Arrays.asList(invocation.arguments()));
            args.add(0, label);

            if (sender instanceof Player velocityPlayer) {
                if (this.command.getPermission() == null || sender.hasPermission(this.command.getPermission())) {
                    try {
                        return this.command.tabComplete(parent.server.getOnlinePlayer(velocityPlayer.getUniqueId()), args);
                    } catch (Exception e) {
                        parent.getServer().getPlugin().getLogger().error("Error while tab-completing command {}.", label, e);

                        return List.of();
                    }
                } else {
                    return List.of();
                }
            } else {
                try {
                    return this.command.tabComplete(parent.server.getConsole(), args);
                } catch (Exception e) {
                    parent.getServer().getPlugin().getLogger().error("Error while tab-completing command {}.", label, e);

                    return List.of();
                }
            }
        }

        @Override
        public boolean hasPermission(@NotNull Invocation invocation) {
            CommandSource sender = invocation.source();
            return this.command.getPermission() == null || sender.hasPermission(this.command.getPermission());
        }

        private static @NotNull List<String> alias(@NotNull String uName, @NotNull List<String> uAliases, boolean distinguishServer) {
            ArrayList<String> aliases = new ArrayList<>();

            for (String alias : uAliases) {
                aliases.add((distinguishServer ? "p" : "") + alias.toLowerCase());
            }

            return aliases;
        }
    }

    protected final @NotNull ECVelocityServer server;

    protected final @NotNull Map<String, ECCommand> commands = new HashMap<>();
    protected final @NotNull Map<String, CommandInter> interCommands = new HashMap<>();

    public ECVelocityCommandManager(@NotNull ECVelocityServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final ECVelocityCommandManager parent = ECVelocityCommandManager.this;

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
                                    try {
                                        ecCommand.run(player, args, false);
                                    } catch (Exception e) {
                                        parent.getServer().getPlugin().getLogger().error("Error while running command {}.", command, e);
                                    }
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

    public @NotNull ECVelocityServer getServer() {
        return this.server;
    }

    @Override
    public @NotNull List<ECCommand> getAll() {
        return List.copyOf(this.commands.values());
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
            CommandInter interCommand = new CommandInter(command, forwardToOther);

            this.commands.put(command.getName().toLowerCase(), command);
            this.interCommands.put(command.getName().toLowerCase(), interCommand);

            CommandMeta.Builder commandMeta = this.server.getHandle().getCommandManager().metaBuilder((distinguishServer ? "p" : "") + command.getName().toLowerCase());
            commandMeta = commandMeta.aliases(CommandInter.alias(command.getName(), command.getAlias(), distinguishServer).toArray(new String[] { }));
            commandMeta = commandMeta.plugin(this.server.getPlugin().getHandle());
            // TODO       this.setDescription(command.getDescription());

            this.server.getHandle().getCommandManager().register(commandMeta.build(), interCommand);

            // TODO       for (String permission : command.getExtraPermissions()) {
            //                if (this.server.getHandle().getPluginManager().getPermission(permission) == null) {
            //                    this.server.getHandle().getPluginManager().addPermission(new Permission(permission));
            //                }
            //            }

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is already registered");
        }
    }

    @Override
    public @NotNull ECCommand unregister(@NotNull ECCommand command) {
        if (this.commands.containsKey(command.getName().toLowerCase())) {
            this.server.getHandle().getCommandManager().unregister(command.getName().toLowerCase());

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