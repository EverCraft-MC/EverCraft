package io.github.evercraftmc.core;

import io.github.evercraftmc.core.api.ECModule;
import io.github.evercraftmc.core.api.ECModuleInfo;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerJoinEvent;
import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.messaging.ECMessenger;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.bjsl.parsers.YamlParser;
import io.github.kale_ko.bjsl.processor.ObjectProcessor;
import io.github.kale_ko.ejcl.file.bjsl.StructuredBJSLFileConfig;
import io.github.kale_ko.ejcl.mysql.StructuredMySQLConfig;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ECPlugin {
    private static class MessagingDetails {
        public String host = "127.0.0.1";
        public short port = 3000;

        public UUID id = null;
    }

    private static class MySQLDetails {
        public String host;
        public short port;

        public String username;
        public String password;

        public String database;
    }

    protected final @NotNull Object handle;

    protected final @NotNull File pluginFile;
    protected final @NotNull File dataDirectory;

    protected final @NotNull ECEnvironment environment;
    protected ECServer server;

    protected final @NotNull Logger logger;
    protected final @NotNull ClassLoader classLoader;

    protected StructuredMySQLConfig<ECPlayerData> data;
    protected ECMessenger messenger;

    protected ParsedObject translations;

    public ECPlugin(@NotNull Object handle, @NotNull File pluginFile, @NotNull File dataDirectory, @NotNull ECEnvironment environment, @NotNull Logger logger, @NotNull ClassLoader classLoader) {
        this.handle = handle;

        this.pluginFile = pluginFile;
        this.dataDirectory = dataDirectory;

        this.environment = environment;

        this.logger = logger;
        this.classLoader = classLoader;

        ECPluginManager.registerPlugin(this);
    }

    private final @NotNull Map<Path, ECModuleInfo> moduleInfoMap = new HashMap<>();
    private final @NotNull Map<String, Path> fileMap = new HashMap<>();
    private final @NotNull Map<String, Boolean> loadedMap = new HashMap<>();

    public void load() {
        this.logger.info("Loading plugin EverCraft-Core..");

        ECPluginManager.registerPlugin(this);

        try {
            if (!Files.exists(this.getDataDirectory().toPath())) {
                Files.createDirectories(this.getDataDirectory().toPath());
            }
        } catch (Exception e) {
            this.logger.error("Failed to create data dir", e);
            return;
        }

        try {
            StructuredBJSLFileConfig<MessagingDetails> messagingDetails = new StructuredBJSLFileConfig.Builder<>(MessagingDetails.class, this.dataDirectory.toPath().resolve("messaging.yml").toFile(), new YamlParser.Builder().build()).build();
            messagingDetails.load(true);
            if (messagingDetails.get().id == null) {
                messagingDetails.get().id = UUID.fromString(System.getProperty("serverID"));
                messagingDetails.save();
            }

            this.messenger = new ECMessenger(this, new InetSocketAddress(messagingDetails.get().host, messagingDetails.get().port), messagingDetails.get().id);
            this.messenger.start();
        } catch (Exception e) {
            this.logger.error("Failed connecting to Messaging server", e);
            return;
        }

        try {
            StructuredBJSLFileConfig<MySQLDetails> mysqlDetails = new StructuredBJSLFileConfig.Builder<>(MySQLDetails.class, this.dataDirectory.toPath().resolve("mysql.yml").toFile(), new YamlParser.Builder().build()).build();
            mysqlDetails.load(true);

            this.logger.info("Connecting to MySQL server..");

            this.data = new StructuredMySQLConfig.Builder<>(ECPlayerData.class, mysqlDetails.get().host, mysqlDetails.get().port, mysqlDetails.get().database, "evercraft").setUsername(mysqlDetails.get().username).setPassword(mysqlDetails.get().password).setUseMariadb(true).setProcessor(new ObjectProcessor.Builder().setIgnoreNulls(true).setIgnoreEmptyObjects(true).setIgnoreDefaults(true).build()).build();
            this.data.connect();

            this.logger.info("Loading plugin data..");

            this.data.load(false);

            this.server.getScheduler().runTaskRepeat(this::loadData, 60 * 20, 120 * 20);

            this.logger.info("Loaded plugin data");
        } catch (Exception e) {
            this.logger.error("Failed loading plugin data", e);
            return;
        }

        try {
            this.logger.info("Downloading minecraft translations..");

            String response;
            try (HttpClient httpClient = HttpClient.newBuilder().build()) {
                HttpRequest httpRequest = HttpRequest.newBuilder(new URI("https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/" + this.getServer().getMinecraftVersion() + "/assets/minecraft/lang/en_us.json")).GET().build();
                response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
            }

            this.translations = BJSL.parseJson(response).asObject();

            this.logger.info("Downloaded minecraft translations for en_us.");
        } catch (Exception e) {
            this.logger.error("Failed downloading minecraft translations", e);
        }

        try {
            Path modulesDirectory = this.getDataDirectory().toPath().resolve("modules");
            if (!Files.exists(modulesDirectory)) {
                Files.createDirectories(modulesDirectory);
            }

            try (Stream<Path> stream = Files.list(modulesDirectory).filter(a -> a.getFileName().toString().toLowerCase().endsWith(".jar"))) {
                List<Path> files = stream.toList();

                for (Path file : files) {
                    ECModuleInfo moduleInfo = null;

                    try (JarInputStream jar = new JarInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
                        ZipEntry entry;
                        while ((entry = jar.getNextEntry()) != null) {
                            if (entry.getName().equalsIgnoreCase("evercraft.yml")) {
                                StringBuilder string = new StringBuilder();

                                int read;
                                while ((read = jar.read()) != -1) {
                                    string.appendCodePoint(read);
                                }

                                moduleInfo = BJSL.parseYaml(string.toString(), ECModuleInfo.class);
                            }
                        }
                    }

                    if (moduleInfo != null) {
                        this.moduleInfoMap.put(file, moduleInfo);
                        this.fileMap.put(moduleInfo.getName().toLowerCase(), file);
                    } else {
                        this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Jar does not contain a module file (evercraft.yml)");
                    }
                }

                for (Path file : files) {
                    ECModuleInfo moduleInfo = this.moduleInfoMap.get(file);
                    this.loadModule(file, moduleInfo);
                }
            }
        } catch (IOException e) {
            this.logger.error("Failed loading modules", e);
        }

        this.logger.info("Finished loading.");
    }

    protected boolean loadModule(@NotNull Path file, @NotNull ECModuleInfo moduleInfo) {
        if (this.loadedMap.containsKey(moduleInfo.getName().toLowerCase())) {
            return this.loadedMap.get(moduleInfo.getName().toLowerCase());
        }

        if (moduleInfo.getName() == null || moduleInfo.getVersion() == null || moduleInfo.getEntry() == null) {
            this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Module info is missing fields");

            this.loadedMap.put(moduleInfo.getName().toLowerCase(), false);
            return false;
        }

        if (moduleInfo.getEnvironment() != null && !(moduleInfo.getEnvironment().trim().equalsIgnoreCase(this.getEnvironment().toString()) || moduleInfo.getEnvironment().trim().equalsIgnoreCase(this.getEnvironment().getType().toString()))) {
            this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Module is in incorrect environment");

            this.loadedMap.put(moduleInfo.getName().toLowerCase(), false);
            return false;
        }

        if (moduleInfo.getDepends() != null) {
            for (String depend : moduleInfo.getDepends()) {
                if (depend.equalsIgnoreCase("Core")) {
                    continue;
                }

                Path file2 = this.fileMap.get(depend.toLowerCase());
                ECModuleInfo moduleInfo2 = this.moduleInfoMap.get(file2);

                boolean loaded = this.loadModule(file2, moduleInfo2);
                if (!loaded) {
                    this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Dependency \"" + moduleInfo2.getName() + "\" failed to load");

                    this.loadedMap.put(moduleInfo.getName().toLowerCase(), false);
                    return false;
                }
            }
        }

        try {
            ECModuleClassLoader moduleClassLoader = new ECModuleClassLoader(file);
            moduleClassLoader.catalogAll();
            Class<?> moduleClass = moduleClassLoader.loadClass(moduleInfo.getEntry());

            if (ECModule.class.isAssignableFrom(moduleClass)) {
                ECModule module = null;
                for (Constructor<?> constructor : moduleClass.getDeclaredConstructors()) {
                    if (constructor.getParameterCount() == 2 && constructor.getParameterTypes()[0] == ECPlugin.class && constructor.getParameterTypes()[1] == ECModuleInfo.class) {
                        try {
                            constructor.setAccessible(true);
                            module = (ECModule) constructor.newInstance(this, moduleInfo);
                            break;
                        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ignored) {
                        }
                    }
                }

                if (module != null) {
                    try {
                        this.logger.info("Enabling module " + module.getInfo().getName() + " v" + module.getInfo().getVersion() + "..");

                        ECPluginManager.registerModule(module);

                        module.load();

                        this.logger.info("Enabled module " + module.getInfo().getName());

                        this.loadedMap.put(moduleInfo.getName().toLowerCase(), true);
                        return true;
                    } catch (Exception e) {
                        this.logger.error("Error loading module \"" + file.getFileName() + "\"", e);
                    }
                } else {
                    this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Entry class has no matching constructor");
                }
            } else {
                this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Entry class does not extend ECModule");
            }
        } catch (IOException e) {
            this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Failed to load module (\"" + moduleInfo.getEntry() + "\")");
        } catch (ClassNotFoundException e) {
            this.logger.error("Error loading module \"" + file.getFileName() + "\"\n  Entry class could not be found (\"" + moduleInfo.getEntry() + "\")");
        }

        this.loadedMap.put(moduleInfo.getName().toLowerCase(), false);
        return false;
    }

    public void unload() {
        for (ECModule module : List.copyOf(ECPluginManager.getModules())) {
            try {
                this.logger.info("Disabling module " + module.getInfo().getName() + " v" + module.getInfo().getVersion());

                module.unload();

                ECPluginManager.unregisterModule(module);

                this.logger.info("Disabled module " + module.getInfo().getName());
            } catch (Exception e) {
                this.logger.error("Error unloading module \"" + module.getName() + "\"", e);
            }
        }

        try {
            if (this.data != null) {
                this.data.close();
            }

            if (this.messenger != null) {
                this.messenger.stop();
            }
        } catch (IOException e) {
            this.logger.error("Error unloading", e);
        }

        ECPluginManager.unregisterPlugin();
    }

    public @NotNull Object getHandle() {
        return this.handle;
    }

    public @NotNull File getPluginFile() {
        return this.pluginFile;
    }

    public @NotNull File getDataDirectory() {
        return this.dataDirectory;
    }

    public @NotNull ECEnvironment getEnvironment() {
        return this.environment;
    }

    public @NotNull ECEnvironmentType getEnvironmentType() {
        return this.environment.getType();
    }

    public ECServer getServer() {
        return this.server;
    }

    public void setServer(@NotNull ECServer server) {
        this.server = server;

        this.server.getEventManager().register(new ECListener() {
            private final @NotNull ECPlugin parent = ECPlugin.this;

            @SuppressWarnings("DataFlowIssue")
            @Override
            public @NotNull ECModule getModule() {
                return null;
            }

            @ECHandler
            public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
                parent.getPlayerData().players.get(event.getPlayer().getUuid().toString()).uuid = event.getPlayer().getUuid();
                parent.getPlayerData().players.get(event.getPlayer().getUuid().toString()).name = event.getPlayer().getName();

                if (parent.getPlayerData().players.get(event.getPlayer().getUuid().toString()).nickname == null) {
                    parent.getPlayerData().players.get(event.getPlayer().getUuid().toString()).nickname = event.getPlayer().getName();
                }

                parent.saveData();

                event.getPlayer().setOnlineDisplayName(event.getPlayer().getDisplayName());
            }
        });
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }

    public @NotNull ECMessenger getMessenger() {
        return this.messenger;
    }

    public @NotNull ECPlayerData getPlayerData() {
        return this.data.get();
    }

    public void setPlayerData(@NotNull ECPlayerData value) {
        this.data.set(value);
    }

    public void loadData() {
        try {
            this.data.load(false);
        } catch (IOException e) {
            this.logger.error("Failed to load player data", e);
        }
    }

    public void saveData() {
        try {
            this.data.save();
        } catch (IOException e) {
            this.logger.error("Failed to save player data", e);
        }
    }

    public @NotNull ParsedObject getTranslations() {
        return this.translations;
    }
}