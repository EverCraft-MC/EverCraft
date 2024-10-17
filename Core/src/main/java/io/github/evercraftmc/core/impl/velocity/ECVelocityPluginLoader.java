package io.github.evercraftmc.core.impl.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.velocity.server.ECVelocityServer;
import java.io.File;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ECVelocityPluginLoader {
    protected final @NotNull ProxyServer server;
    protected final @NotNull Path dataDirectory;
    protected final @NotNull Logger logger;

    private final ECPlugin plugin;

    @Inject
    public ECVelocityPluginLoader(@NotNull ProxyServer server, @NotNull Logger logger, @DataDirectory @NotNull Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

        this.plugin = new ECPlugin(this, new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).toPath(), this.getDataDirectory().getParent().resolve("EverCraft"), ECEnvironment.VELOCITY, this.logger, this.getClass().getClassLoader());
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        this.plugin.setServer(new ECVelocityServer(this.plugin, this.server));

        this.plugin.load();
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
        this.plugin.unload();
    }

    public @NotNull ProxyServer getServer() {
        return this.server;
    }

    public @NotNull Path getDataDirectory() {
        return this.dataDirectory;
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }
}