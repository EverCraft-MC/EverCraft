package io.github.evercraftmc.core.impl.waterfall;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.waterfall.server.ECWaterfallServer;
import net.md_5.bungee.api.plugin.Plugin;

public class ECWaterfallPluginLoader extends Plugin {
    private ECPlugin plugin;

    @Override
    public void onLoad() {
        this.plugin = new ECPlugin(this, this.getFile(), this.getProxy().getPluginsFolder().toPath().resolve("EverCraft").toFile(), ECEnvironment.WATERFALL, this.getSLF4JLogger(), this.getClass().getClassLoader());
    }

    @Override
    public void onEnable() {
        this.plugin.setServer(new ECWaterfallServer(this.plugin, this.getProxy()));

        this.plugin.load();
    }

    @Override
    public void onDisable() {
        this.plugin.unload();
    }
}