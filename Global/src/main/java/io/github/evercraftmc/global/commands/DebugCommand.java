package io.github.evercraftmc.global.commands;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.global.GlobalModule;
import io.github.kale_ko.bjsl.BJSL;
import io.github.kale_ko.bjsl.elements.ParsedElement;
import io.github.kale_ko.bjsl.elements.ParsedObject;
import io.github.kale_ko.ejcl.PathResolver;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements ECCommand {
    protected final @NotNull GlobalModule parent;

    public DebugCommand(@NotNull GlobalModule parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull String getName() {
        return "debug";
    }

    @Override
    public @NotNull List<String> getAlias() {
        return List.of();
    }

    @Override
    public @NotNull String getDescription() {
        return "Get/set data";
    }

    @Override
    public @NotNull String getUsage() {
        return "/debug (get|set) <path> [<value>]";
    }

    @Override
    public @NotNull String getUsage(@NotNull ECPlayer player) {
        return this.getUsage();
    }

    @Override
    public @NotNull String getPermission() {
        return "evercraft.global.commands.debug";
    }

    public @NotNull List<String> getExtraPermissions() {
        return List.of(this.getPermission());
    }

    @Override
    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback) {
        if (args.size() > 1) {
            if (args.get(0).equalsIgnoreCase("get")) {
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 1; i < args.size(); i++) {
                    pathBuilder.append(args.get(i)).append(" ");
                }
                String path = pathBuilder.toString().trim();
                for (ECPlayer player2 : parent.getPlugin().getServer().getPlayers()) {
                    path = Pattern.compile("\\{" + player2.getName() + "}", Pattern.CASE_INSENSITIVE).matcher(path).replaceAll(player2.getUuid().toString());
                }

                ParsedObject dataElement = BJSL.elementify(parent.getPlugin().getPlayerData()).asObject();
                ParsedElement resolvedElement = PathResolver.resolveElement(dataElement, path);

                player.sendMessage(ECTextFormatter.translateColors("&aGot " + path + ":&r\n") + (resolvedElement != null ? BJSL.stringifyJson(resolvedElement) : "undefined"));
                return true;
            } else if (args.get(0).equalsIgnoreCase("set")) {
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 1; i < args.size() - 1; i++) {
                    pathBuilder.append(args.get(i)).append(" ");
                }
                String path = pathBuilder.toString().trim();
                for (ECPlayer player2 : parent.getPlugin().getServer().getPlayers()) {
                    path = Pattern.compile("\\{" + player2.getName() + "}", Pattern.CASE_INSENSITIVE).matcher(path).replaceAll(player2.getUuid().toString());
                }
                String value = args.get(args.size() - 1);

                ParsedObject dataElement = BJSL.elementify(parent.getPlugin().getPlayerData()).asObject();
                ParsedElement valueElement = BJSL.parseJson(value);
                PathResolver.updateElement(dataElement, path, valueElement, true);
                ECPlayerData data = BJSL.parse(dataElement, ECPlayerData.class);
                if (data == null) {
                    player.sendMessage(ECTextFormatter.translateColors("&cError updating data."));
                    return false;
                }
                parent.getPlugin().setPlayerData(data);
                parent.getPlugin().saveData();

                player.sendMessage(ECTextFormatter.translateColors("&aSet " + path + ":&r\n") + BJSL.stringifyJson(valueElement));
                return true;
            } else if (sendFeedback) {
                player.sendMessage(ECTextFormatter.translateColors("&cExpected \"get\" or \"set\"."));
                return false;
            }
        } else if (sendFeedback) {
            player.sendMessage(ECTextFormatter.translateColors("&cYou must pass a command and path."));
            return false;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args) {
        if (args.size() == 1) {
            return List.of("get", "set");
        } else {
            return List.of();
        }
    }
}