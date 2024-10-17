package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.events.messaging.MessageEvent;
import io.github.evercraftmc.messaging.client.ECMessagingClient;
import io.github.evercraftmc.messaging.common.ECMessageId;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ECMessenger {
    protected final @NotNull ECPlugin parent;

    protected final @NotNull InetSocketAddress address;

    protected final @NotNull UUID id;

    protected ECMessagingClient client;

    public ECMessenger(@NotNull ECPlugin parent, @NotNull InetSocketAddress address, @NotNull UUID id) {
        this.parent = parent;

        this.address = address;

        this.id = id;
    }

    public @NotNull InetSocketAddress getAddress() {
        return this.address;
    }

    public @NotNull UUID getId() {
        return this.id;
    }

    public boolean isRunning() {
        return this.client.isRunning();
    }

    public void start() {
        ECAllMessageId.register();
        ECUUIDMessageId.register();
        ECEnvironmentMessageId.register();
        ECEnvironmentTypeMessageId.register();

        this.client = new ECMessagingClient(parent.getLogger(), this.address);
        this.client.setListener(message -> {
            parent.getServer().getEventManager().emit(new MessageEvent(this, message));
        });
        this.client.start();
    }

    public void stop() {
        if (this.client != null) {
            this.client.stop();
        }
    }

    public void send(@NotNull ECMessageId receiver, byte @NotNull [] data) {
        this.client.send(new ECUUIDMessageId(this.id), receiver, data);
    }
}