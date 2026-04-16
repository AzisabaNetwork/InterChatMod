package net.azisaba.interchatmod.fabric;

import com.terraformersmc.modmenu.api.ModMenuApi;
import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.CommandManager;
import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.common.model.Guild;
import net.azisaba.interchatmod.common.model.GuildMember;
import net.azisaba.interchatmod.fabric.entity.FabricClientActor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Mod extends InterChatMod implements ModInitializer, ModMenuApi {
    public static Mod instance;
    public static final Timer TIMER = new Timer(true);
    public static final Set<Guild> GUILDS = Collections.synchronizedSet(new HashSet<>());
    public static final Map<Long, Set<GuildMember>> guildMembers = new ConcurrentHashMap<>();
    public static final Map<UUID, File> images = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        instance = this;
        ModConfig.load();
        initialize();
        CommandManager.forEachCommand(command ->
                ClientCommandManager.DISPATCHER.register(command.builder(this).getUnsafeLiteralBuilder()));
    }

    public static boolean isInAzisaba() {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) return false;
        return serverInfo.address.endsWith(".azisaba.net") || serverInfo.address.equals("azisaba.net");
    }

    public void trySwitch() {
        if (ModConfig.apiKey.isEmpty()) return;
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

    @Override
    public @NotNull ModConfigAccessor getConfig() {
        return ModConfigAccessorImpl.INSTANCE;
    }

    @Override
    public @NotNull Actor adaptActor(@NotNull Object actor) {
        return new FabricClientActor((FabricClientCommandSource) actor);
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }

    @Override
    public @NotNull AbstractWebSocketChatClient createWebSocketChatClient(@NotNull URI uri) {
        return new WebSocketChatClient(this, uri);
    }

    @Override
    public boolean supportsSecureConnection() {
        return false;
    }
}
