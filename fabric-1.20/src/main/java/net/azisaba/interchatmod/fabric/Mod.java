package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Mod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("InterChatMod");
    public static final net.azisaba.interchatmod.fabric.ModConfig CONFIG = net.azisaba.interchatmod.fabric.ModConfig.createAndLoad();
    public static final Timer TIMER = new Timer(true);
    public static final Set<Guild> GUILDS = Collections.synchronizedSet(new HashSet<>());
    public static final Map<Long, Set<GuildMember>> guildMembers = new ConcurrentHashMap<>();
    public static final Map<UUID, File> images = new ConcurrentHashMap<>();
    public static WebSocketChatClient client;

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(Commands.builderGTell());
            dispatcher.register(Commands.builderGS());
            dispatcher.register(Commands.builderG());
            dispatcher.register(Commands.builderReconnectInterChat());
            dispatcher.register(Commands.builderGuild());
        });

        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (CONFIG.apiKey().isEmpty()) return;
                try {
                    Gson gson = new Gson();
                    JsonArray arr = gson.fromJson(makeRequest("interchat/guilds/list"), JsonArray.class);
                    Set<Guild> localGuilds = getGuildsFromArray(arr);
                    GUILDS.clear();
                    GUILDS.addAll(localGuilds);
                    for (Guild guild : localGuilds) {
                        JsonArray membersArray = gson.fromJson(makeRequest("interchat/guilds/" + guild.id() + "/members"), JsonArray.class);
                        guildMembers.put(guild.id(), GuildMember.getGuildMembersFromArray(membersArray));
                    }
                    for (Set<GuildMember> memberSet : guildMembers.values()) {
                        for (GuildMember member : memberSet) {
                            Commands.KNOWN_PLAYERS.add(member.name());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to fetch guild list", e);
                }
            }
        }, 1000 * 30, 1000 * 30);

        reconnect();
    }

    public static String getEffectiveApiHost() {
        return CONFIG.apiHost().isEmpty() ? Constants.DEFAULT_API_HOST : CONFIG.apiHost();
    }

    private static String makeRequest(String path) throws IOException, URISyntaxException {
        String url = "https://" + getEffectiveApiHost() + "/" + path;
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + CONFIG.apiKey());
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String uploadImage(byte[] data) throws IOException, URISyntaxException {
        String url = "https://" + getEffectiveApiHost() + "/interchat/upload_image";
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + CONFIG.apiKey());
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "image/png");
        connection.getOutputStream().write(data);
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void reconnect() {
        try {
            if (client != null) {
                client.close();
            }
            LOGGER.info("Attempting to connect to the server");
            URI uri = new URI("wss://" + getEffectiveApiHost() + "/interchat/stream?server=dummy");
            client = new WebSocketChatClient(uri);
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
        if (CONFIG.apiKey().isEmpty()) return;
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
