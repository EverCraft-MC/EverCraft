package com.kale_ko.kalesutilities.commands.player;

import com.kale_ko.kalesutilities.Main;
import com.kale_ko.kalesutilities.Util;
import com.kale_ko.kalesutilities.Config;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class NicknameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (Util.hasPermission(sender, "kalesutilities.setnickname")) {
            if (args.length > 0) {
                Config config = Config.load("players.yml");
                YamlConfiguration data = config.getConfig();

                if (sender instanceof Player player) {
                    data.set("players." + player.getPlayer().getName() + ".nickname", args[0]);

                    config.save();

                    Util.sendMessage(sender, Main.Instance.config.getConfig().getString("messages.setnickname"));
                    Util.updatePlayerName(player);
                } else {
                    Util.sendMessage(sender, Main.Instance.config.getConfig().getString("messages.noconsole"));
                }
            } else {
                Util.sendMessage(sender, Main.Instance.config.getConfig().getString("messages.usage").replace("{usage}", Main.Instance.getCommand("nickname").getUsage()));
            }
        } else {
            Util.sendMessage(sender, Main.Instance.config.getConfig().getString("messages.noperms").replace("{permission}", "kalesutilities.setnickname"));
        }

        return true;
    }
}