package io.github.evercraftmc.messaging.server.netty;

import io.github.evercraftmc.messaging.server.ECMessagingServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

public class ECMessagingServerHandler extends ChannelDuplexHandler {
    protected final @NotNull ECMessagingServer parent;

    public ECMessagingServerHandler(@NotNull ECMessagingServer parent) {
        this.parent = parent;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof ByteBuf buffer) {
            buffer.retain();

            try {
                for (Channel channel : parent.getChannelGroup()) {
                    parent.getConnectionWorker().submit(() -> {
                        try {
                            ByteBuf copy = buffer.copy();
                            copy.retain();

                            try {
                                channel.writeAndFlush(copy).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                            } finally {
                                copy.release();
                            }
                        } catch (Exception e) {
                            ctx.fireExceptionCaught(e);
                        }
                    });
                }
            } finally {
                buffer.release();
            }
        } else {
            throw new RuntimeException("Incorrect type passed to " + this.getClass().getSimpleName() + ", " + msg.getClass().getName());
        }
    }

    @Override
    public void channelReadComplete(@NotNull ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void write(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ChannelPromise promise) {
        if (msg instanceof ByteBuf buffer) {
            ctx.write(buffer);
        } else {
            throw new RuntimeException("Incorrect type passed to " + this.getClass().getSimpleName() + ", " + msg.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable e) {
        parent.getLogger().error("Exception in Messaging server", e);
    }
}