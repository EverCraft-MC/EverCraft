package com.kale_ko.evercraft.spigot.commands.warp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.kale_ko.evercraft.shared.util.StringUtils;
import com.kale_ko.evercraft.shared.util.formatting.TextFormatter;
import com.kale_ko.evercraft.spigot.SpigotMain;
import com.kale_ko.evercraft.spigot.commands.SpigotCommand;
import com.kale_ko.evercraft.spigot.util.types.SerializableLocation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelWarpCommand extends SpigotCommand {
    public DelWarpCommand(String name, String description, List<String> aliases, String permission) {
        super(name, description, aliases, permission);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (args.length >= 1) {
                if (SpigotMain.getInstance().getWarps().getSerializable(args[0], SerializableLocation.class) != null) {
                    SpigotMain.getInstance().getWarps().set(args[0], null);
                    SpigotMain.getInstance().getWarps().save();

                    player.sendMessage(TextFormatter.translateColors(SpigotMain.getInstance().getPluginMessages().getString("warp.delWarp").replace("{warp}", args[0])));
                } else {
                    sender.sendMessage(TextFormatter.translateColors(SpigotMain.getInstance().getPluginMessages().getString("error.invalidArgs")));
                }
            } else {
                sender.sendMessage(TextFormatter.translateColors(SpigotMain.getInstance().getPluginMessages().getString("error.invalidArgs")));
            }
        } else {
            sender.sendMessage(TextFormatter.translateColors(SpigotMain.getInstance().getPluginMessages().getString("error.noConsole")));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<String>();

        if (args.length == 1) {
            list = new ArrayList<String>(SpigotMain.getInstance().getWarps().getKeys(false));
        } else {
            return Arrays.asList();
        }

        if (args.length > 0) {
            return StringUtils.matchPartial(args[args.length - 1], list);
        } else {
            return list;
        }
    }
}