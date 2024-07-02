package io.github.evercraftmc.core;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ECPlayerData {
    // Core
    public Map<String, Player> players = new HashMap<>();

    // Moderation
    public boolean chatLocked = false;
    public boolean maintenance = false;

    public static class Player {
        // Core
        public UUID uuid;
        public String name;

        public String displayName;
        public String prefix = null;

        // Global
        public InetAddress lastIp = null;

        public Instant firstJoin = null;
        public Instant lastJoin = null;
        public long playTime = 0;

        // Moderation
        public boolean staffChat = false;
        public boolean commandSpy = false;

        public Moderation ban = null;
        public Moderation mute = null;

        public static class Moderation {
            public UUID moderator;
            public String reason;

            public Instant date;
            public Instant until;
        }

        private Player() {
            this(null, null);
        }

        public Player(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;

            this.displayName = name;
        }
    }
}