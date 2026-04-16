package net.azisaba.interchatmod.fabric;

import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.command.CommandManager;
import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.fabric.entity.FabricClientActor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.TimerTask;

public class Mod extends InterChatMod implements ModInitializer {
    public static final ModConfig CONFIG = new ModConfig();
    public static Mod instance;
    private static final ModConfigAccessor CONFIG_ACCESSOR = new ModConfigAccessorImpl(CONFIG);

    @Override
    public void onInitialize() {
        instance = this;
        CONFIG.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                CommandManager.forEachCommand(command ->
                        dispatcher.register(command.builder(this).getUnsafeLiteralBuilder())));

        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            if (!CONFIG.chatWithoutCommand) {
                return true;
            }
            if (message.startsWith("!")) {
                return message.length() != 1;
            }
            try {
                getWebSocketChatClient().sendMessageToGuild(null, message);
            } catch (WebsocketNotConnectedException e) {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.sendMessage(Text.translatable("generic.not_connected"), false);
                reconnect();
            }
            return false;
        });

        ClientSendMessageEvents.MODIFY_CHAT.register((message) -> {
            if (!CONFIG.chatWithoutCommand) {
                return message;
            }
            if (message.startsWith("!")) {
                return message.substring(1);
            }
            return message;
        });

        getTimer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    trySwitch();
                } catch (Exception e) {
                    getLogger().error("Failed to switch", e);
                }
            }
        }, 2000, 2000);

        initialize();
    }

    public static boolean isInAzisaba() {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) return false;
        return serverInfo.address.endsWith(".azisaba.net") || serverInfo.address.equals("azisaba.net");
    }

    public void trySwitch() {
        if (CONFIG.apiKey.isEmpty()) return;
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
        return CONFIG_ACCESSOR;
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
}
