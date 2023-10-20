package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.azisaba.interchatmod.fabric.model.Guild;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Mod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("InterChatMod");
    public static final net.azisaba.interchatmod.fabric.ModConfig CONFIG = net.azisaba.interchatmod.fabric.ModConfig.createAndLoad();
    public static final Timer TIMER = new Timer(true);
    public static final Set<Guild> GUILDS = Collections.synchronizedSet(new HashSet<>());
    public static WebSocketChatClient client;

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(Commands.builderGS());
            dispatcher.register(Commands.builderG());
            dispatcher.register(Commands.builderReconnectInterChat());
        });

        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String url = "https://api-ktor.azisaba.net/interchat/guilds/list";
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.addRequestProperty("Authorization", "Bearer " + CONFIG.apiKey());
                    String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    JsonArray arr = new Gson().fromJson(response, JsonArray.class);
                    Set<Guild> localGuilds = getGuildsFromArray(arr);
                    GUILDS.clear();
                    GUILDS.addAll(localGuilds);
                } catch (Exception e) {
                    LOGGER.warn("Failed to fetch guild list", e);
                }
            }
        }, 1000 * 30, 1000 * 30);

        reconnect();
    }

    public static void reconnect() {
        try {
            if (client != null) {
                client.close();
            }
            LOGGER.info("Attempting to connect to the server");
            URI uri = new URI("wss://api-ktor.azisaba.net/interchat/stream?server=dummy");
            client = new WebSocketChatClient(uri);
            if (uri.getScheme().startsWith("wss")) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                client.setSocketFactory(factory);
            }
            client.connect();
        } catch (Exception e) {
            LOGGER.error("Failed to establish WebSocket session", e);
        }
    }

    @NotNull
    private static Set<Guild> getGuildsFromArray(JsonArray arr) {
        Set<Guild> localGuilds = new HashSet<>();
        for (JsonElement element : arr) {
            JsonObject obj = element.getAsJsonObject();
            localGuilds.add(
                    new Guild(
                            obj.get("id").getAsLong(),
                            obj.get("name").getAsString(),
                            obj.get("format").getAsString(),
                            obj.get("capacity").getAsInt(),
                            obj.get("open").getAsBoolean(),
                            obj.get("deleted").getAsBoolean()
                    )
            );
        }
        return localGuilds;
    }

    public static boolean isInAzisaba() {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) return false;
        return serverInfo.address.endsWith(".azisaba.net") || serverInfo.address.equals("azisaba.net");
    }

    public static void trySwitch() {
        if (client == null) return;
        ServerInfo serverData = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverData != null) {
            client.switchServer(serverData.address);
        }
        IntegratedServer singleServer = MinecraftClient.getInstance().getServer();
        if (singleServer != null) {
            client.switchServer(singleServer.getSaveProperties().getLevelName());
        }
    }
}
