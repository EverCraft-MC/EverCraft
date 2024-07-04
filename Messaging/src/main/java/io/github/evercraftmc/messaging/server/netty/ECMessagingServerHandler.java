package io.github.evercraftmc.messaging.server.netty;

import io.github.evercraftmc.messaging.server.ECMessagingServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

public class ECMessagingServerHandler extends ChannelDuplexHandler {
    protected final @NotNull ECMessagingServer parent;

    public ECMessagingServerHandler(@NotNull ECMessagingServer parent) {
        this.parent = parent;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        for (Channel channel : parent.getChannelGroup()) {
            parent.getConnectionWorker().submit(() -> {
                channel.writeAndFlush(msg);
            });
        }
    }

    @Override
    public void channelReadComplete(@NotNull ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void write(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ChannelPromise promise) {
        ctx.write(msg);
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable e) {
        parent.getLogger().error("Exception in Messaging server", e);
    }
}