package com.kale_ko.evercraft.bungee.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.kale_ko.evercraft.bungee.BungeePlugin;
import com.kale_ko.evercraft.bungee.Util;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class GlobalMesageListener implements Listener {
    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (event.getTag().equals("BungeeCord")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

            String channel = in.readUTF();

            if (channel.equals("globalChat")) {
                String sender = in.readUTF();
                String message = in.readUTF();

                Util.messageServers("globalChat", sender, message);

                BungeePlugin.Instance.bot.sendMessage(Util.discordFormating(message));
            } else if (channel.equals("globalStaffChat")) {
                Util.messageServers("globalStaffChat", in.readUTF(), in.readUTF());
            } else if (channel.equals("globalCommandspy")) {
                Util.messageServers("globalCommandspy", in.readUTF(), in.readUTF());
            }
        }
    }
}