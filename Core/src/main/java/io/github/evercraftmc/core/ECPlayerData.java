package io.github.evercraftmc.core;

import io.github.kale_ko.bjsl.processor.annotations.Rename;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECPlayerData {
    // Core
    public @NotNull Map<String, Player> players = new HashMap<>();

    // Moderation
    public boolean chatLocked = false;
    public boolean maintenance = false;

    public static class Player {
        // Core
        public @NotNull UUID uuid;
        public @NotNull String name;

        @Rename("displayName") public String nickname;
        public String prefix;

        // Global
        public InetAddress lastIp = null;

        public Instant firstJoin = null;
        public Instant lastJoin = null;
        public long playTime = 0;

        // Moderation
        public boolean staffChatEnabled = false;
        public boolean commandSpyEnabled = false;

        public @Nullable Moderation currentBan = null;
        public @Nullable Moderation currentMute = null;

        public static class Moderation {
            public UUID moderator;
            public String reason;

            public Instant date;
            public Instant until;
        }

        @SuppressWarnings("DataFlowIssue")
        private Player() { // Required for serialization
            this(null, null);
        }

        public Player(@NotNull UUID uuid, @NotNull String name) {
            this.uuid = uuid;
            this.name = name;

            this.nickname = name;
            this.prefix = null;
        }
    }
}