package net.azisaba.interchatmod.common;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.common.util.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InterChatMod {
    protected static final Logger LOGGER = LoggerFactory.getLogger("InterChatMod");
    private boolean initialized = false;
    protected final @NotNull Timer timer = new Timer(true);
    protected final @NotNull List<@NotNull String> knownPlayers = new ArrayList<>();
    protected final @NotNull Set<@NotNull Guild> guilds = Collections.synchronizedSet(new HashSet<>());
    protected final @NotNull Map<@NotNull Long, @NotNull Set<@NotNull GuildMember>> guildMembers = new ConcurrentHashMap<>();
    protected final @NotNull Map<@NotNull String, @NotNull File> screenshots = new ConcurrentHashMap<>();
    protected @Nullable AbstractWebSocketChatClient client;

    public abstract @NotNull ModConfigAccessor getConfig();

    public abstract @NotNull Actor adaptActor(@NotNull Object actor);

    public abstract void execute(@NotNull Runnable runnable);

    public abstract @NotNull AbstractWebSocketChatClient createWebSocketChatClient(@NotNull URI uri);

    public boolean supportsSecureConnection() {
        return true;
    }

    public void initialize() {
        if (initialized) throw new IllegalStateException("Already initialized");
        initialized = true;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getConfig().getApiKey().isEmpty()) return;
                try {
                    Gson gson = new Gson();
                    JsonArray arr = gson.fromJson(makeRequest("interchat/guilds/list"), JsonArray.class);
                    Set<Guild> localGuilds = Guild.getGuildsFromArray(arr);
                    getGuilds().clear();
                    getGuilds().addAll(localGuilds);
                    for (Guild guild : localGuilds) {
                        JsonArray membersArray = gson.fromJson(makeRequest("interchat/guilds/" + guild.id() + "/members"), JsonArray.class);
                        guildMembers.put(guild.id(), GuildMember.getGuildMembersFromArray(membersArray));
                    }
                    for (Set<GuildMember> memberSet : guildMembers.values()) {
                        for (GuildMember member : memberSet) {
                            getKnownPlayers().add(member.name());
                        }
                    }
                } catch (Exception e) {
                    getLogger().error("Failed to fetch guild list", e);
                }
            }
        }, 1000 * 30, 1000 * 30);

        reconnect();
    }

    protected @NotNull String makeRequest(String path) throws IOException {
        String protocol = supportsSecureConnection() ? "https" : "http";
        String url = protocol + "://" + getConfig().getEffectiveApiHost() + "/" + path;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + getConfig().getApiKey());
        return ByteStreams.readString(connection.getInputStream(), StandardCharsets.UTF_8);
    }

    public void reconnect() {
        try {
            if (client != null) {
                client.close();
            }
            System.out.println("Attempting to connect to the server");
            // it has to be insecure url, java 8 does not have a required ssl certificate,
            // and we had to disable Automatic HTTPS Rewrites on Cloudflare settings :<
            String protocol = supportsSecureConnection() ? "wss" : "ws";
            URI uri = new URI(protocol + "://" + getConfig().getEffectiveApiHost() + "/interchat/stream?server=dummy");
            client = createWebSocketChatClient(uri);
            client.connectBlocking();
        } catch (Exception e) {
            getLogger().error("Failed to establish WebSocket session", e);
        }
    }

    /**
     * Uploads an image to the InterChat server and returns the URL.
     * @param data Image data
     * @return Image URL
     * @throws IOException If an I/O error occurs
     */
    public @NotNull String uploadImage(byte @NotNull [] data) throws IOException {
        String protocol = supportsSecureConnection() ? "https" : "http";
        String url = protocol + "://" + getConfig().getEffectiveApiHost() + "/interchat/upload_image";
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + getConfig().getApiKey());
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "image/png");
        connection.getOutputStream().write(data);
        return new String(ByteStreams.readFully(connection.getInputStream()), StandardCharsets.UTF_8);
    }

    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    public @NotNull List<@NotNull String> getKnownPlayers() {
        return knownPlayers;
    }

    public @NotNull Set<@NotNull Guild> getGuilds() {
        return guilds;
    }

    public @NotNull Map<@NotNull Long, @NotNull Set<@NotNull GuildMember>> getGuildMembers() {
        return guildMembers;
    }

    public @NotNull Map<@NotNull String, @NotNull File> getScreenshots() {
        return screenshots;
    }

    public @NotNull Timer getTimer() {
        return timer;
    }

    public @NotNull AbstractWebSocketChatClient getWebSocketChatClient() {
        return Objects.requireNonNull(client);
    }
}
