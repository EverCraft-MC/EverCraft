package io.github.evercraftmc.core.impl.paper.server;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.events.ECEvent;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.events.player.PlayerCommandEvent;
import io.github.evercraftmc.core.api.server.ECEventManager;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.paper.server.player.ECPaperPlayer;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.lang.reflect.Method;
import java.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ECPaperEventManager implements ECEventManager {
    protected class PaperListener implements Listener {
        protected final @NotNull ECPaperEventManager parent = ECPaperEventManager.this;

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerJoin(@NotNull PlayerLoginEvent event) {
            if (!parent.server.getPlugin().getPlayerData().players.containsKey(event.getPlayer().getUniqueId().toString())) {
                parent.server.getPlugin().getPlayerData().players.put(event.getPlayer().getUniqueId().toString(), new ECPlayerData.Player(event.getPlayer().getUniqueId(), event.getPlayer().getName()));
            }

            io.github.evercraftmc.core.api.events.player.PlayerLoginEvent newEvent = new io.github.evercraftmc.core.api.events.player.PlayerLoginEvent(new ECPaperPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString())));
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.getPlayer().kick(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
            Component ogMessage = event.joinMessage();
            io.github.evercraftmc.core.api.events.player.PlayerJoinEvent newEvent = new io.github.evercraftmc.core.api.events.player.PlayerJoinEvent(new ECPaperPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), ogMessage != null ? ECComponentFormatter.componentToString(ogMessage) : "");
            parent.emit(newEvent);

            event.joinMessage(Component.empty());

            if (newEvent.isCancelled()) {
                event.getPlayer().kick(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            } else if (!newEvent.getJoinMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getJoinMessage());
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
            Component ogMessage = event.quitMessage();
            io.github.evercraftmc.core.api.events.player.PlayerLeaveEvent newEvent = new io.github.evercraftmc.core.api.events.player.PlayerLeaveEvent(new ECPaperPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), ogMessage != null ? ECComponentFormatter.componentToString(ogMessage) : "");
            parent.emit(newEvent);

            event.quitMessage(Component.empty());

            if (!newEvent.getLeaveMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getLeaveMessage());
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerChat(@NotNull AsyncChatEvent event) {
            String message = ECComponentFormatter.componentToString(event.message());
            if (message.isEmpty()) {
                event.setCancelled(true);
                return;
            }

            ECPaperPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            PlayerChatEvent newEvent = new PlayerChatEvent(player, PlayerChatEvent.MessageType.CHAT, message, new ArrayList<>(parent.getServer().getOnlinePlayers()));
            parent.emit(newEvent);

            event.message(Component.empty());
            event.setCancelled(true);

            if (newEvent.isCancelled()) {
                if (!newEvent.getCancelReason().isEmpty()) {
                    player.sendMessage(newEvent.getCancelReason());
                }
            } else if (!newEvent.getMessage().isEmpty()) {
                for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getServer().getOnlinePlayers())) {
                    player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                }
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
            String message = event.getMessage();
            if (message.isEmpty() || message.equalsIgnoreCase("/")) {
                event.setCancelled(true);
                return;
            }

            ECPaperPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            PlayerCommandEvent newEvent = new PlayerCommandEvent(player, message);
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.setCancelled(true);

                if (!newEvent.getCancelReason().isEmpty()) {
                    player.sendMessage(newEvent.getCancelReason());
                }
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerChat(@NotNull PlayerDeathEvent event) {
            Component component = event.deathMessage();
            if (component == null) {
                return;
            }
            String message = ECComponentFormatter.componentToString(component);

            ECPaperPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            PlayerChatEvent newEvent = new PlayerChatEvent(player, PlayerChatEvent.MessageType.DEATH, message, new ArrayList<>(parent.getServer().getOnlinePlayers()));
            parent.emit(newEvent);

            event.deathMessage(Component.empty());

            if (newEvent.isCancelled()) {
                if (!newEvent.getCancelReason().isEmpty()) {
                    player.sendMessage(newEvent.getCancelReason());
                }
            } else if (!newEvent.getMessage().isEmpty()) {
                for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getServer().getOnlinePlayers())) {
                    player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                }
            }
        }

        @EventHandler(priority=EventPriority.HIGH)
        public void onPlayerChat(@NotNull PlayerAdvancementDoneEvent event) {
            Component component = event.message();
            if (component == null || event.getAdvancement().getDisplay() == null || !event.getAdvancement().getDisplay().doesAnnounceToChat() || Boolean.FALSE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS))) {
                return;
            }
            String message = ECComponentFormatter.componentToString(component);

            ECPaperPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            PlayerChatEvent newEvent = new PlayerChatEvent(player, PlayerChatEvent.MessageType.ADVANCEMENT, message, new ArrayList<>(parent.getServer().getOnlinePlayers()));
            parent.emit(newEvent);

            event.message(Component.empty());

            if (newEvent.isCancelled()) {
                if (!newEvent.getCancelReason().isEmpty()) {
                    player.sendMessage(newEvent.getCancelReason());
                }
            } else if (!newEvent.getMessage().isEmpty()) {
                for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getServer().getOnlinePlayers())) {
                    player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                }
            }
        }
    }

    protected final @NotNull ECPaperServer server;

    protected final @NotNull Map<Class<? extends ECEvent>, List<Map.Entry<ECListener, Method>>> listeners = new HashMap<>();

    public ECPaperEventManager(@NotNull ECPaperServer server) {
        this.server = server;

        this.server.getHandle().getPluginManager().registerEvents(new PaperListener(), (Plugin) this.server.getPlugin().getHandle());
    }

    public @NotNull ECPaperServer getServer() {
        return this.server;
    }

    @Override
    public void emit(@NotNull ECEvent event) {
        if (this.listeners.containsKey(event.getClass())) {
            for (Map.Entry<ECListener, Method> entry : this.listeners.get(event.getClass())) {
                try {
                    entry.getValue().setAccessible(true);
                    entry.getValue().invoke(entry.getKey(), event);
                } catch (Exception e) {
                    this.getServer().getPlugin().getLogger().error("Error while invoking event handler {}.", entry.getValue(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener register(@NotNull ECListener listener) {
        if (listener instanceof org.bukkit.event.Listener paperListener) {
            this.server.getHandle().getPluginManager().registerEvents(paperListener, (Plugin) this.server.getPlugin().getHandle());
        }

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ECHandler.class)) {
                if (method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    if (!this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                        this.listeners.put((Class<? extends ECEvent>) method.getParameterTypes()[0], new ArrayList<>());
                    }
                    this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).add(new AbstractMap.SimpleEntry<>(listener, method));

                    this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).sort(Comparator.comparingInt(a -> a.getValue().getDeclaredAnnotationsByType(ECHandler.class)[0].order().getValue()));
                } else {
                    this.getServer().getPlugin().getLogger().warn("Warning registered listener, method is annotated with ECHandler but is not valid! {}.", method);
                }
            }
        }

        return listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener unregister(@NotNull ECListener listener) {
        if (listener instanceof org.bukkit.event.Listener paperListener) {
            HandlerList.unregisterAll(paperListener);
        }

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ECHandler.class) && method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0]) && this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).remove(new AbstractMap.SimpleEntry<>(listener, method));
            }
        }

        return listener;
    }

    @Override
    public void unregisterAll() {
        for (Map.Entry<Class<? extends ECEvent>, List<Map.Entry<ECListener, Method>>> classEntry : this.listeners.entrySet()) {
            for (ECListener listener : classEntry.getValue().stream().map(Map.Entry::getKey).toList()) {
                this.unregister(listener);
            }
        }
    }
}