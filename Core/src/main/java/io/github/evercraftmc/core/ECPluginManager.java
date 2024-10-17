package io.github.evercraftmc.core;

import io.github.evercraftmc.core.api.ECModule;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class ECPluginManager {
    protected static ECPlugin plugin;

    protected static final @NotNull Map<Class<? extends ECModule>, ECModule> modules = new HashMap<>();

    private ECPluginManager() {
    }

    public static ECPlugin getPlugin() {
        return ECPluginManager.plugin;
    }

    public static @NotNull ECPlugin registerPlugin(@NotNull ECPlugin plugin) {
        ECPluginManager.plugin = plugin;
        return plugin;
    }

    public static @Nullable ECPlugin unregisterPlugin() {
        ECPlugin oldPlugin = ECPluginManager.plugin;
        ECPluginManager.plugin = null;
        return oldPlugin;
    }

    public static @NotNull @Unmodifiable Collection<ECModule> getModules() {
        return Collections.unmodifiableCollection(ECPluginManager.modules.values());
    }

    @SuppressWarnings("unchecked")
    public static <T extends ECModule> T getModule(@NotNull Class<T> clazz) {
        if (ECPluginManager.modules.containsKey(clazz)) {
            return (T) ECPluginManager.modules.get(clazz);
        } else {
            throw new RuntimeException("Module \"" + clazz.getSimpleName() + "\" is not registered");
        }
    }

    public static <T extends ECModule> @NotNull T registerModule(@NotNull T module) {
        if (!ECPluginManager.modules.containsKey(module.getClass())) {
            ECPluginManager.modules.put(module.getClass(), module);

            return module;
        } else {
            throw new RuntimeException("Module \"" + module.getClass().getSimpleName() + "\" is already registered");
        }
    }

    public static <T extends ECModule> @NotNull T unregisterModule(@NotNull T module) {
        if (ECPluginManager.modules.containsKey(module.getClass())) {
            ECPluginManager.modules.remove(module.getClass());

            return module;
        } else {
            throw new RuntimeException("Module \"" + module.getClass().getSimpleName() + "\" is not registered");
        }
    }
}