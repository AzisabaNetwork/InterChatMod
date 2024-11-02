package net.azisaba.interchatmod.blueberry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.azisaba.interchatmod.blueberry.commands.GuildChatCommand;
import net.azisaba.interchatmod.blueberry.commands.GuildCommand;
import net.azisaba.interchatmod.blueberry.commands.GuildFocusCommand;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.blueberrymc.client.commands.ClientCommandManager;
import net.blueberrymc.client.event.player.ClientLocalPlayerChatEvent;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.config.VisualConfigManager;
import net.blueberrymc.common.bml.event.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.jetbrains.annotations.Contract;
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

public class Mod extends BlueberryMod {
    public WebSocketChatClient client;
    public Set<Guild> guilds = Collections.synchronizedSet(new HashSet<>());
    public Map<Long, Set<GuildMember>> guildMembers = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        if (!Blueberry.isClient()) {
            throw new RuntimeException("This mod cannot be installed on server");
        }
        ClientCommandManager.register("cgf", new GuildFocusCommand("cgf", this));
        ClientCommandManager.register("cgs", new GuildFocusCommand("cgs", this));
        ClientCommandManager.register("cg", new GuildChatCommand(this));
        ClientCommandManager.register("cguild", new GuildCommand(this));
    }

    @Override
    public void onPreInit() {
        Blueberry.getEventManager().registerEvents(this, this);
        try {
            getConfig().reloadConfig();
        } catch (IOException e) {
            getLogger().warn("Failed to reload config", e);
        }
        VisualConfigManager.load(getConfig(), ICConfig.class);
        setVisualConfig(VisualConfigManager.createFromClass(ICConfig.class));
        getVisualConfig().onSave(vc -> {
            VisualConfigManager.save(getConfig(), vc);
            try {
                getConfig().saveConfig();
            } catch (IOException e) {
                getLogger().warn("Failed to save config", e);
            }
            onReload();
        });
    }

    @Override
    public boolean onReload() {
        try {
            getConfig().reloadConfig();
        } catch (IOException e) {
            getLogger().warn("Failed to reload config", e);
        }
        VisualConfigManager.load(getConfig(), ICConfig.class);
        reconnect();
        return false;
    }

    public void reconnect() {
        try {
            if (client != null) {
                client.close();
            }
            URI uri;
            if (ICConfig.endpointHostOverride == null || ICConfig.endpointHostOverride.isBlank()) {
                uri = new URI("wss://api-ktor.azisaba.net/interchat/stream?server=dummy");
            } else {
                uri = new URI(ICConfig.endpointHostOverride + "/interchat/stream?server=dummy");
            }
            client = new WebSocketChatClient(uri, this, Minecraft.getInstance());
            if (uri.getScheme().startsWith("wss")) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                client.setSocketFactory(factory);
            }
            client.connect();
        } catch (Exception e) {
            getLogger().error("Failed to establish WebSocket session", e);
        }
    }

    @Override
    public void onPostInit() {
        reconnect();

        Blueberry.getUtil().getClientScheduler().runTaskTimerAsynchronously(this, 1000 * 5, 1000 * 30, () -> {
            try {
                Gson gson = new Gson();
                JsonArray arr = gson.fromJson(makeRequest("interchat/guilds/list"), JsonArray.class);
                Set<Guild> localGuilds = Guild.getGuildsFromArray(arr);
                guilds.clear();
                guilds.addAll(localGuilds);
                for (Guild guild : localGuilds) {
                    JsonArray membersArray = gson.fromJson(makeRequest("interchat/guilds/" + guild.id() + "/members"), JsonArray.class);
                    guildMembers.put(guild.id(), GuildMember.getGuildMembersFromArray(membersArray));
                }
                for (Set<GuildMember> memberSet : guildMembers.values()) {
                    for (GuildMember member : memberSet) {
                        GuildCommand.KNOWN_PLAYERS.add(member.name());
                    }
                }
            } catch (Exception e) {
                getLogger().warn("Failed to fetch guild list", e);
            }
        });
    }

    @Contract("_ -> new")
    private static @NotNull String makeRequest(String path) throws IOException {
        String url = "https://api-ktor.azisaba.net/" + path;
        if (ICConfig.apiHostOverride != null && !ICConfig.apiHostOverride.isBlank()) {
            url = ICConfig.apiHostOverride + "/" + path;
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + ICConfig.apiKey);
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @EventHandler
    public void onScreenChanged(ScreenChangedEvent e) {
        trySwitch();
    }

    public static boolean isInAzisaba() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();;
        if (serverData == null) return false;
        return serverData.ip.endsWith(".azisaba.net") || serverData.ip.equals("azisaba.net");
    }

    public void trySwitch() {
        if (client == null) return;
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData != null) {
            client.switchServer(serverData.ip);
        }
        IntegratedServer singleServer = Minecraft.getInstance().getSingleplayerServer();
        if (singleServer != null) {
            client.switchServer(singleServer.getWorldData().getLevelName());
        }
    }

    @EventHandler
    public void onChat(ClientLocalPlayerChatEvent e) {
        if (!ICConfig.chatWithoutCommand) return;
        if (e.getMessage().startsWith("!")) {
            e.setMessage(e.getMessage().substring(1));
            return;
        }
        e.setCancelled(true);
        try {
            client.sendMessageToGuild(null, e.getMessage());
        } catch (WebsocketNotConnectedException ex) {
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("ギルドチャットに接続されていません。"));
            reconnect();
        }
    }
}
