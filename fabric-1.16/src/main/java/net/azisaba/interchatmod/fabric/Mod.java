package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.ByteStreams;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Mod implements ModInitializer, ModMenuApi {
    public static final Timer TIMER = new Timer(true);
    public static final Set<Guild> GUILDS = Collections.synchronizedSet(new HashSet<>());
    public static final Map<Long, Set<GuildMember>> guildMembers = new ConcurrentHashMap<>();
    public static WebSocketChatClient client;

    @Override
    public void onInitialize() {
        ClientCommandManager.DISPATCHER.register(Commands.builderGS());
        ClientCommandManager.DISPATCHER.register(Commands.builderG());
        ClientCommandManager.DISPATCHER.register(Commands.builderReconnectInterChat());
        ClientCommandManager.DISPATCHER.register(Commands.builderGuild());

        ModConfig.load();

        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
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
                } catch (Exception e) {
                    System.err.println("Failed to fetch guild list");
                    e.printStackTrace();
                }
            }
        }, 1000 * 30, 1000 * 30);

        reconnect();
    }

    private static @NotNull String makeRequest(String path) throws IOException {
        String url = "https://api-ktor.azisaba.net/" + path;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + ModConfig.apiKey);
        return ByteStreams.readString(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    public static void reconnect() {
        try {
            if (client != null) {
                client.close();
            }
            System.out.println("Attempting to connect to the server");
            // it has to be insecure url, java 8 does not have required ssl certificate,
            // and we had to disable Automatic HTTPS Rewrites on Cloudflare settings :<
            URI uri = new URI("ws://api-ktor.azisaba.net/interchat/stream?server=dummy");
            client = new WebSocketChatClient(uri);
            if (uri.getScheme().startsWith("wss")) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                client.setSocketFactory(factory);
            }
            client.connectBlocking();
        } catch (Exception e) {
            System.err.println("Failed to establish WebSocket session");
            e.printStackTrace();
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
