package io.github.evercraftmc.core.impl.spigot.server;

import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.core.api.server.ECCommandManager;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.spigot.server.util.ECSpigotComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.messaging.ECMessage;
import io.github.evercraftmc.core.messaging.ECMessageType;
import io.github.evercraftmc.core.messaging.ECRecipient;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ECSpigotCommandManager implements ECCommandManager {
    protected class CommandInter extends Command {
        protected final ECSpigotCommandManager parent = ECSpigotCommandManager.this;

        protected ECCommand command;
        protected boolean forwardToOther;

        public CommandInter(ECCommand command, boolean distinguishServer, boolean forwardToOther) {
            super((distinguishServer ? "s" : "") + command.getName());

            this.setName((distinguishServer ? "s" : "") + command.getName().toLowerCase());
            this.setDescription(command.getDescription());
            this.setAliases(CommandInter.alias(command.getName(), command.getAlias(), distinguishServer));
            this.setPermission(command.getPermission().toLowerCase());

            this.command = command;
            this.forwardToOther = forwardToOther;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (sender instanceof Player spigotPlayer) {
                if (sender.hasPermission(this.getPermission())) {
                    this.command.run(parent.server.getOnlinePlayer(spigotPlayer.getUniqueId()), args, true);

                    if (this.forwardToOther) {
                        try {
                            ByteArrayOutputStream commandMessageData = new ByteArrayOutputStream();
                            DataOutputStream commandMessage = new DataOutputStream(commandMessageData);
                            commandMessage.writeInt(ECMessageType.GLOBAL_COMMAND.getCode());
                            commandMessage.writeUTF(spigotPlayer.getUniqueId().toString());
                            commandMessage.writeUTF(this.getName());
                            commandMessage.close();

                            parent.server.getPlugin().getMessager().send(ECRecipient.fromEnvironmentType(ECEnvironmentType.PROXY), commandMessageData.toByteArray());
                        } catch (IOException e) {
                            parent.server.getPlugin().getLogger().error("[Messager] Failed to send message", e);
                        }
                    }
                } else {
                    sender.sendMessage(ECSpigotComponentFormatter.stringToComponent(ECTextFormatter.translateColors("&cYou do not have permission to run that command")));
                }
            } else {
                this.command.run(parent.server.getConsole(), args, true);
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args) {
            if (sender instanceof Player spigotPlayer) {
                if (sender.hasPermission(this.getPermission())) {
                    return this.command.tabComplete(parent.server.getOnlinePlayer(spigotPlayer.getUniqueId()), args);
                } else {
                    return List.of();
                }
            } else {
                return this.command.tabComplete(parent.server.getConsole(), args);
            }
        }

        private static List<String> alias(String uName, List<String> uAliases, boolean distinguishServer) {
            ArrayList<String> aliases = new ArrayList<String>();

            aliases.add("evercraft:" + (distinguishServer ? "s" : "") + uName.toLowerCase());

            for (String alias : uAliases) {
                aliases.add((distinguishServer ? "s" : "") + alias.toLowerCase());
                aliases.add("evercraft:" + (distinguishServer ? "s" : "") + alias.toLowerCase());
            }

            return aliases;
        }
    }

    protected ECSpigotServer server;

    protected Map<String, ECCommand> commands = new HashMap<String, ECCommand>();
    protected Map<String, CommandInter> interCommands = new HashMap<String, CommandInter>();

    public ECSpigotCommandManager(ECSpigotServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final ECSpigotCommandManager parent = ECSpigotCommandManager.this;

            @ECHandler
            public void onMessage(MessageEvent event) {
                ECMessage message = event.getMessage();

                if (!message.getSender().matches(parent.server) && message.getRecipient().matches(parent.server)) {

                }
            }
        });
    }

    public ECSpigotServer getServer() {
        return this.server;
    }

    @Override
    public ECCommand get(String name) {
        return this.commands.get(name.toLowerCase());
    }

    @Override
    public ECCommand register(ECCommand command) {
        return this.register(command, false);
    }

    @Override
    public ECCommand register(ECCommand command, boolean distinguishServer) {
        return this.register(command, distinguishServer, !distinguishServer);
    }

    @Override
    public ECCommand register(ECCommand command, boolean distinguishServer, boolean forwardToOther) {
        String name = command.getName();
        if (distinguishServer) {
            name = "s" + name;
        }

        if (!this.commands.containsKey(name)) {
            CommandInter interCommand = new CommandInter(command, distinguishServer, forwardToOther);

            this.commands.put(name, command);
            this.interCommands.put(name, interCommand);

            this.server.getHandle().getCommandMap().register("evercraft", interCommand);
            this.interCommands.get(name).register(this.server.getHandle().getCommandMap());

            return command;
        } else {
            throw new RuntimeException("Command /" + command.getName() + " is already registered");
        }
    }

    @Override
    public ECCommand unregister(ECCommand command) {
        if (this.commands.containsKey(command.getName().toLowerCase())) {
            this.interCommands.get(command.getName().toLowerCase()).unregister(this.server.getHandle().getCommandMap());

            this.commands.remove(command.getName().toLowerCase());
            this.interCommands.remove(command.getName().toLowerCase());

            return command;
        } else if (this.commands.containsKey("s" + command.getName().toLowerCase())) {
            this.interCommands.get("s" + command.getName().toLowerCase()).unregister(this.server.getHandle().getCommandMap());

            this.commands.remove("s" + command.getName().toLowerCase());
            this.interCommands.remove("s" + command.getName().toLowerCase());

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