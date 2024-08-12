package io.github.evercraftmc.moderation;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.ECModule;
import io.github.evercraftmc.core.api.ECModuleInfo;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.moderation.commands.*;
import io.github.evercraftmc.moderation.listeners.*;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModerationModule extends ECModule {
    protected final @NotNull List<ECCommand> commands = new ArrayList<>();
    protected final @NotNull List<ECListener> listeners = new ArrayList<>();

    protected ModerationModule(@NotNull ECPlugin plugin, @NotNull ECModuleInfo info) {
        super(plugin, info);
    }

    public void load() {
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new KickCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new BanCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new UnbanCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new MuteCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new UnmuteCommand(this), false, false));

        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new MaintenanceCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new ClearChatCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new LockChatCommand(this), false, false));

        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new StaffChatCommand(this), false, false));
        this.commands.add(this.getPlugin().getServer().getCommandManager().register(new CommandSpyCommand(this), false, false));

        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new BanListener(this)));
        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new MuteListener(this)));
        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new MaintenanceListener(this)));
        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new LockChatListener(this)));
        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new StaffChatListener(this)));
        this.listeners.add(this.getPlugin().getServer().getEventManager().register(new CommandSpyListener(this)));
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