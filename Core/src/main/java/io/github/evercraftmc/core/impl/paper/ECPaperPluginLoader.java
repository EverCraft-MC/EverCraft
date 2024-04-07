package io.github.evercraftmc.core.impl.paper;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.paper.server.ECPaperServer;
import org.bukkit.plugin.java.JavaPlugin;

public class ECPaperPluginLoader extends JavaPlugin {
    private ECPlugin plugin;

    @Override
    public void onLoad() {
        this.plugin = new ECPlugin(this, this.getFile(), this.getServer().getPluginsFolder().toPath().resolve("EverCraft").toFile(), ECEnvironment.PAPER, this.getSLF4JLogger(), this.getClassLoader());
    }

    @Override
    public void onEnable() {
        this.plugin.setServer(new ECPaperServer(this.plugin, this.getServer()));

        this.plugin.load();
    }

    @Override
    public void onDisable() {
        this.plugin.unload();
    }
}