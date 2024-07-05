package io.github.evercraftmc.messaging.client.netty;

import io.github.evercraftmc.messaging.client.ECMessagingClient;
import io.github.evercraftmc.messaging.common.ECMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

public class ECMessagingEventHandler extends ChannelInboundHandlerAdapter {
    protected final @NotNull ECMessagingClient parent;

    public ECMessagingEventHandler(@NotNull ECMessagingClient parent) {
        this.parent = parent;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof ECMessage message) {
            if (parent.getListener() != null) {
                parent.getListener().onMessage(message);
            }
        } else {
            throw new RuntimeException("Incorrect type passed to " + this.getClass().getSimpleName() + ", " + msg.getClass().getName());
        }
    }
}