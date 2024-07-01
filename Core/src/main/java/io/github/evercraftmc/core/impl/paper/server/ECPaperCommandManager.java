package io.github.evercraftmc.core.impl.paper.server;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECPaperCommandManager implements ECCommandManager {
    protected class CommandInter extends Command {
        protected final @NotNull ECPaperCommandManager parent = ECPaperCommandManager.this;

        protected final @NotNull ECCommand command;
        protected final boolean forwardToOther;

        public CommandInter(@NotNull ECCommand command, boolean distinguishServer, boolean forwardToOther) {
            super((distinguishServer ? "s" : "") + command.getName());

            this.setName((distinguishServer ? "s" : "") + command.getName().toLowerCase());
            this.setDescription(command.getDescription());
            this.setAliases(CommandInter.alias(command.getName(), command.getAlias(), distinguishServer));
            this.setPermission(command.getPermission() != null ? command.getPermission().toLowerCase() : null);

            this.command = command;
            this.forwardToOther = forwardToOther;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
            if (sender instanceof Player paperPlayer) {
                if (this.getPermission() == null || sender.hasPermission(this.getPermission()) || sender.isOp()) {
                    this.command.run(parent.server.getOnlinePlayer(paperPlayer.getUniqueId()), Arrays.asList(args), true);

                    if (this.forwardToOther) {
                        try {
                            ByteArrayOutputStream commandMessageData = new ByteArrayOutputStream();
                            DataOutputStream commandMessage = new DataOutputStream(commandMessageData);
                            commandMessage.writeInt(ECMessageType.GLOBAL_COMMAND);
                            commandMessage.writeUTF(paperPlayer.getUniqueId().toString());
                            commandMessage.writeUTF(this.getName());
                            commandMessage.writeInt(args.length);
                            for (String arg : args) {
                                commandMessage.writeUTF(arg);
                            }
                            commandMessage.close();

                            parent.server.getPlugin().getMessenger().send(ECRecipient.fromEnvironmentType(ECEnvironmentType.PROXY), commandMessageData.toByteArray());
                        } catch (IOException e) {
                            parent.server.getPlugin().getLogger().error("[Messenger] Failed to send message", e);
                        }
                    }
                } else {
                    sender.sendMessage(ECComponentFormatter.stringToComponent(ECTextFormatter.translateColors("&cYou do not have permission to run that command")));
                }
            } else {
                this.command.run(parent.server.getConsole(), Arrays.asList(args), true);
            }

            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player paperPlayer) {
                if (this.getPermission() == null || sender.hasPermission(this.getPermission()) || sender.isOp()) {
                    return this.command.tabComplete(parent.server.getOnlinePlayer(paperPlayer.getUniqueId()), Arrays.asList(args));
                } else {
                    return List.of();
                }
            } else {
                return this.command.tabComplete(parent.server.getConsole(), Arrays.asList(args));
            }
        }

        private static @NotNull List<String> alias(@NotNull String uName, @NotNull List<String> uAliases, boolean distinguishServer) {
            ArrayList<String> aliases = new ArrayList<>();

            aliases.add("evercraft:" + (distinguishServer ? "s" : "") + uName.toLowerCase());

            for (String alias : uAliases) {
                aliases.add((distinguishServer ? "s" : "") + alias.toLowerCase());
                aliases.add("evercraft:" + (distinguishServer ? "s" : "") + alias.toLowerCase());
            }

            return aliases;
        }
    }

    protected final @NotNull ECPaperServer server;

    protected final @NotNull Map<String, ECCommand> commands = new HashMap<>();
    protected final @NotNull Map<String, CommandInter> interCommands = new HashMap<>();

    public ECPaperCommandManager(@NotNull ECPaperServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final ECPaperCommandManager parent = ECPaperCommandManager.this;

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

    public @NotNull ECPaperServer getServer() {
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
            CommandInter interCommand = new CommandInter(command, distinguishServer, forwardToOther);

            this.commands.put(command.getName().toLowerCase(), command);
            this.interCommands.put(command.getName().toLowerCase(), interCommand);

            this.server.getHandle().getCommandMap().register("evercraft", interCommand);
            this.interCommands.get(command.getName().toLowerCase()).register(this.server.getHandle().getCommandMap());

            for (String permission : command.getExtraPermissions()) {
                if (this.server.getHandle().getPluginManager().getPermission(permission) == null) {
                    this.server.getHandle().getPluginManager().addPermission(new Permission(permission));
                }
            }

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is already registered");
        }
    }

    @Override
    public @NotNull ECCommand unregister(@NotNull ECCommand command) {
        if (this.commands.containsKey(command.getName().toLowerCase())) {
            this.interCommands.get(command.getName().toLowerCase()).unregister(this.server.getHandle().getCommandMap());

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