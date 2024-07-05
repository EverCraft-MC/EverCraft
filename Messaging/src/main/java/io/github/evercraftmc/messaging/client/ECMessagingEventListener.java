package io.github.evercraftmc.messaging.client;

import io.github.evercraftmc.messaging.common.ECMessage;
import org.jetbrains.annotations.NotNull;

public interface ECMessagingEventListener {
    public void onMessage(@NotNull ECMessage message);
}