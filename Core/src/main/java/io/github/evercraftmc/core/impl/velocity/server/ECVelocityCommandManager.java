package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import io.github.evercraftmc.core.api.ECModule;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.core.api.server.ECCommandManager;
import io.github.evercraftmc.core.api.server.player.ECConsole;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.messaging.ECEnvironmentTypeMessageId;
import io.github.evercraftmc.core.messaging.ECMessageType;
import io.github.evercraftmc.messaging.common.ECMessage;
import java.io.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
                if (this.hasPermission(invocation)) {
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
                            commandMessage.writeBoolean(false);
                            commandMessage.writeUTF(velocityPlayer.getUniqueId().toString());
                            commandMessage.writeUTF(this.command.getName());
                            commandMessage.writeInt(args.size());
                            for (String arg : args) {
                                commandMessage.writeUTF(arg);
                            }
                            commandMessage.close();

                            parent.server.getPlugin().getMessenger().send(new ECEnvironmentTypeMessageId(ECEnvironmentType.BACKEND), commandMessageData.toByteArray());
                        } catch (IOException e) {
                            parent.server.getPlugin().getLogger().error("[Messenger] Failed to send message", e);
                        }
                    }
                } else {
                    sender.sendMessage(ECComponentFormatter.stringToComponent(ECTextFormatter.translateColors("&cYou do not have permission to run that command")));
                }
            } else if (sender instanceof ConsoleCommandSource) {
                try {
                    this.command.run(parent.server.getConsole(), args, true);
                } catch (Exception e) {
                    parent.getServer().getPlugin().getLogger().error("Error while running command {}.", label, e);

                    return;
                }

                if (this.forwardToOther) {
                    try {
                        ByteArrayOutputStream commandMessageData = new ByteArrayOutputStream();
                        DataOutputStream commandMessage = new DataOutputStream(commandMessageData);
                        commandMessage.writeInt(ECMessageType.GLOBAL_COMMAND);
                        commandMessage.writeBoolean(true);
                        commandMessage.writeUTF(this.command.getName());
                        commandMessage.writeInt(args.size());
                        for (String arg : args) {
                            commandMessage.writeUTF(arg);
                        }
                        commandMessage.close();

                        parent.server.getPlugin().getMessenger().send(new ECEnvironmentTypeMessageId(ECEnvironmentType.BACKEND), commandMessageData.toByteArray());
                    } catch (IOException e) {
                        parent.server.getPlugin().getLogger().error("[Messenger] Failed to send message", e);
                    }
                }
            }
        }

        @Override
        public List<String> suggest(@NotNull Invocation invocation) {
            CommandSource sender = invocation.source();
            String label = invocation.alias();
            String[] args = invocation.arguments();
            if (args.length == 0) {
                args = new String[] { "" }; // Fixes an issue with velocity where tab completing "/command " would send [] instead of [""]
            }

            if (sender instanceof Player velocityPlayer) {
                if (this.hasPermission(invocation)) {
                    try {
                        List<String> completions = this.command.tabComplete(parent.server.getOnlinePlayer(velocityPlayer.getUniqueId()), Arrays.asList(args));

                        List<String> matches = new ArrayList<>();
                        for (String string : completions) {
                            if (string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                                matches.add(string);
                            }
                        }
                        return matches;
                    } catch (Exception e) {
                        parent.getServer().getPlugin().getLogger().error("Error while tab-completing command {}.", label, e);

                        return List.of();
                    }
                } else {
                    return List.of();
                }
            } else if (sender instanceof ConsoleCommandSource) {
                try {
                    List<String> completions = this.command.tabComplete(parent.server.getConsole(), Arrays.asList(args));

                    List<String> matches = new ArrayList<>();
                    for (String string : completions) {
                        if (string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                            matches.add(string);
                        }
                    }
                    return matches;
                } catch (Exception e) {
                    parent.getServer().getPlugin().getLogger().error("Error while tab-completing command {}.", label, e);

                    return List.of();
                }
            }

            return List.of();
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

    protected final @NotNull Map<String, ECCommand> commandsAndAliases = new HashMap<>();

    public ECVelocityCommandManager(@NotNull ECVelocityServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final ECVelocityCommandManager parent = ECVelocityCommandManager.this;

            @SuppressWarnings("DataFlowIssue")
            @Override
            public @NotNull ECModule getModule() {
                return null;
            }

            @ECHandler
            public void onMessage(@NotNull MessageEvent event) {
                ECMessage message = event.getMessage();

                if (!message.getSender().matches(parent.server) && message.getRecipient().matches(parent.server)) {
                    try {
                        ByteArrayInputStream commandMessageData = new ByteArrayInputStream(message.getData());
                        DataInputStream commandMessage = new DataInputStream(commandMessageData);

                        int type = commandMessage.readInt();
                        if (type == ECMessageType.GLOBAL_COMMAND) {
                            if (!commandMessage.readBoolean()) {
                                UUID uuid = UUID.fromString(commandMessage.readUTF());
                                String command = commandMessage.readUTF();
                                List<String> args = new ArrayList<>();
                                int argC = commandMessage.readInt();
                                for (int i = 0; i < argC; i++) {
                                    args.add(commandMessage.readUTF());
                                }

                                ECPlayer player = parent.server.getOnlinePlayer(uuid);
                                if (player != null) {
                                    ECCommand ecCommand = parent.server.getCommandManager().getByName(command);
                                    if (ecCommand != null) {
                                        try {
                                            ecCommand.run(player, args, false);
                                        } catch (Exception e) {
                                            parent.getServer().getPlugin().getLogger().error("Error while running command {}.", command, e);
                                        }
                                    }
                                }
                            } else {
                                String command = commandMessage.readUTF();
                                List<String> args = new ArrayList<>();
                                int argC = commandMessage.readInt();
                                for (int i = 0; i < argC; i++) {
                                    args.add(commandMessage.readUTF());
                                }

                                ECConsole player = parent.server.getConsole();
                                ECCommand ecCommand = parent.server.getCommandManager().getByName(command);
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
    public @NotNull @Unmodifiable List<ECCommand> getAll() {
        return List.copyOf(this.commands.values());
    }

    @Override
    public @Nullable ECCommand getByName(@NotNull String name) {
        return this.commands.get(name.toLowerCase());
    }

    @Override
    public @Nullable ECCommand getByAlias(@NotNull String alias) {
        return this.commandsAndAliases.get(alias.toLowerCase());
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

            this.commandsAndAliases.put(command.getName().toLowerCase(), command);
            for (String alias : command.getAlias()) {
                this.commandsAndAliases.put(alias.toLowerCase(), command);
            }

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

            this.interCommands.remove(command.getName().toLowerCase());
            this.commands.remove(command.getName().toLowerCase());

            this.commandsAndAliases.remove(command.getName().toLowerCase());
            for (String alias : command.getAlias()) {
                this.commandsAndAliases.remove(alias.toLowerCase());
            }

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is not registered");
        }
    }

    @Override
    public void unregisterAll() {
        for (ECCommand command : List.copyOf(this.commands.values())) {
            this.unregister(command);
        }
    }
}