package com.kale_ko.kalesutilities;

import java.io.File;
import java.nio.file.Paths;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Util {
    public static void sendMessage(CommandSender user, String message) {
        user.sendMessage(formatMessage(Main.Instance.config.getString("config.prefix") + " " + message));
    }

    public static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String unFormatMessage(String message) {
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(chars[i + 1]) > -1) {
                chars[i] = '&';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }

        return new String(chars);
    }

    public static String stripFormating(String message) {
        return ChatColor.stripColor(message);
    }

    public static String styleMessage(String message, String style) {
        return ChatColor.translateAlternateColorCodes('&', style + message);
    }

    public static Boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission) || player.isOp();
    }

    public static Boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.isOp();
    }

    public static String getPlayerNickName(Player player) {
        String name = player.getName();

        File dataFolder = Main.Instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File dataFile = Paths.get(dataFolder.getAbsolutePath(), "players.yml").toFile();

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

        if (data.getString("players." + player.getName() + ".nickname") != null && !data.getString("players." + player.getName() + ".nickname").equalsIgnoreCase("") && !data.getString("players." + player.getName() + ".nickname").equalsIgnoreCase(" ")) {
            if (Util.hasPermission(player, "kalesutilities.nonickstar") || data.getString("players." + player.getName() + ".nickname").equalsIgnoreCase(name) || Util.stripFormating(Util.formatMessage(data.getString("players." + player.getName() + ".nickname") + "&r")).equalsIgnoreCase(name)) {
                name = Util.formatMessage(data.getString("players." + player.getName() + ".nickname") + "&r");
            } else {
                name = Util.formatMessage("*" + data.getString("players." + player.getName() + ".nickname") + "&r");
            }
        }

        return name;
    }

    public static String getPlayerPrefix(Player player) {
        String prefix = "";

        File dataFolder = Main.Instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File dataFile = Paths.get(dataFolder.getAbsolutePath(), "players.yml").toFile();

        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

        if (data.getString("players." + player.getName() + ".prefix") != null && !data.getString("players." + player.getName() + ".prefix").equalsIgnoreCase("") && !data.getString("players." + player.getName() + ".prefix").equalsIgnoreCase(" ")) {
            prefix = Util.formatMessage(data.getString("players." + player.getName() + ".prefix") + "&r") + " ";
        }

        return prefix;
    }

    public static String getPlayerName(Player player) {
        return getPlayerPrefix(player) + getPlayerNickName(player);
    }

    public static void updatePlayerName(Player player) {
        player.setDisplayName(getPlayerName(player));
        player.setPlayerListName(getPlayerName(player));
    }
}