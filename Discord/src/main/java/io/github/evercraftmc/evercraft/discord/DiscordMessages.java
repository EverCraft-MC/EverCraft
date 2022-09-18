package io.github.evercraftmc.evercraft.discord;

import java.util.Map;

public class DiscordMessages {
    public static class Error {
        public String noPerms = "You need the permission \"{permission}\" to do that";
        public String userNotFound = "Couldn't find user \"{player}\"";
        public String invalidArgs = "Invalid arguments";
    }

    public static class Reload {
        public String reloading = "Reloading plugin..";
        public String reloaded = "Successfully reloaded";
    }

    public static class Linking {
        public String success = "Successfully linked your account to {account}!";
        public String needCode = "To link your account to Minecraft, join the server and type /verify";
        public String invalidCode = "That code is not a valid/known code";
    }

    public Error error = new Error();

    public Reload reload = new Reload();

    public Map<String, String> info = Map.ofEntries(Map.entry("about", ""), Map.entry("ip", ""), Map.entry("vote", ""), Map.entry("staff", ""));

    public Linking linking = new Linking();
}