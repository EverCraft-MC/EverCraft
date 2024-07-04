package io.github.evercraftmc.messaging.server;

import io.github.kale_ko.bjsl.parsers.YamlParser;
import io.github.kale_ko.ejcl.file.bjsl.StructuredYamlFileConfig;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ECMessagingServerMain {
    private static class MessagingDetails {
        public String host = "127.0.0.1";
        public int port = 3000;
    }

    public static void main(String @NotNull [] args) {
        Logger logger = LoggerFactory.getLogger("Messaging");

        try {
            logger.info("Loading config...");

            StructuredYamlFileConfig<MessagingDetails> messagingDetails = new StructuredYamlFileConfig<>(MessagingDetails.class, Path.of("messaging.yml").toFile(), new YamlParser.Builder().build());
            messagingDetails.load(true);

            ECMessagingServer server = new ECMessagingServer(logger, new InetSocketAddress(messagingDetails.get().host, messagingDetails.get().port));
            server.start();

            {
                Scanner stdIn = new Scanner(System.in);

                while (true) {
                    String read = stdIn.nextLine();

                    if (read.equalsIgnoreCase("q") || read.equalsIgnoreCase("quit")) {
                        server.stop();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}