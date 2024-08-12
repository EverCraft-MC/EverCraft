package io.github.evercraftmc.global;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.ECModule;
import io.github.evercraftmc.core.api.ECModuleInfo;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.global.commands.*;
import io.github.evercraftmc.global.listeners.ChatListener;
import io.github.evercraftmc.global.listeners.JoinListener;
import io.github.evercraftmc.global.listeners.ServerChoiceListener;
import io.github.evercraftmc.global.listeners.ServerPingListener;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class GlobalModule extends ECModule {
    protected final @NotNull List<ECCommand> commands = new ArrayList<>();
    protected final @NotNull List<ECListener> listeners = new ArrayList<>();

    protected GlobalModule(@NotNull ECPlugin plugin, @NotNull ECModuleInfo info) {
        super(plugin, info);
    }

    public void load() {
        this.commands.add(this.plugin.getServer().getCommandManager().register(new HelpCommand(this), false, false));

        this.commands.add(this.plugin.getServer().getCommandManager().register(new NickCommand(this), false, true));
        this.commands.add(this.plugin.getServer().getCommandManager().register(new PrefixCommand(this), false, true));

        this.commands.add(this.plugin.getServer().getCommandManager().register(new MessageCommand(this), false, false));
        this.commands.add(this.plugin.getServer().getCommandManager().register(new ReplyCommand(this), false, false));

        if (this.plugin.getEnvironment().getType() == ECEnvironmentType.PROXY) {
            this.commands.add(this.plugin.getServer().getCommandManager().register(new ServerCommand(this), false, false));
        }

        this.commands.add(this.plugin.getServer().getCommandManager().register(new DebugCommand(this), true, false));

        this.listeners.add(this.plugin.getServer().getEventManager().register(new JoinListener(this)));
        this.listeners.add(this.plugin.getServer().getEventManager().register(new ChatListener(this)));

        if (this.plugin.getEnvironment().getType() == ECEnvironmentType.PROXY) {
            this.listeners.add(this.plugin.getServer().getEventManager().register(new ServerChoiceListener(this)));
            this.listeners.add(this.plugin.getServer().getEventManager().register(new ServerPingListener(this)));
        }
    }

    public void unload() {
        for (ECCommand command : this.commands) {
            this.plugin.getServer().getCommandManager().unregister(command);
        }

        for (ECListener listener : this.listeners) {
            this.plugin.getServer().getEventManager().unregister(listener);
        }
    }
}