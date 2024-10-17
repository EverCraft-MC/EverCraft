package io.github.evercraftmc.core.impl.paper;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.paper.server.ECPaperServer;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class ECPaperPluginLoader extends JavaPlugin {
    private ECPlugin plugin;

    @Override
    public void onLoad() {
        this.plugin = new ECPlugin(this, new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).toPath(), this.getDataFolder().toPath().getParent().resolve("EverCraft"), ECEnvironment.PAPER, this.getSLF4JLogger(), this.getClass().getClassLoader());
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