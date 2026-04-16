package net.azisaba.interchatmod.forge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.azisaba.interchatmod.common.AbstractWebSocketChatClient;
import net.azisaba.interchatmod.common.InterChatMod;
import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import net.azisaba.interchatmod.common.entity.Actor;
import net.azisaba.interchatmod.forge.entity.ForgeClientActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.command.CommandSource;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

@net.minecraftforge.fml.common.Mod("interchatmod")
public class Mod extends InterChatMod {
    public static Mod instance;

    public Mod() {
        instance = this;
        ModConfig.load();
        MinecraftForge.EVENT_BUS.register(this);
        initialize();
    }

    public static boolean isInAzisaba() {
        ServerData serverData = Minecraft.getInstance().getCurrentServerData();
        if (serverData == null) return false;
        return serverData.serverIP.endsWith(".azisaba.net") || serverData.serverIP.equals("azisaba.net");
    }

    public void trySwitch() {
        if (ModConfig.apiKey.isEmpty()) return;
        if (client == null) return;
        ServerData serverData = Minecraft.getInstance().getCurrentServerData();
        if (serverData != null) {
            client.switchServer(serverData.serverIP);
        }
        IntegratedServer singleServer = Minecraft.getInstance().getIntegratedServer();
        if (singleServer != null) {
            client.switchServer(singleServer.getActiveAnvilConverter().getName());
        }
    }

    @Override
    public @NotNull ModConfigAccessor getConfig() {
        return ModConfigAccessorImpl.INSTANCE;
    }

    @Override
    public @NotNull Actor adaptActor(@NotNull Object actor) {
        return new ForgeClientActor((CommandSource) actor);
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        Minecraft.getInstance().enqueue(runnable);
    }

    @Override
    public @NotNull AbstractWebSocketChatClient createWebSocketChatClient(@NotNull URI uri) {
        return new WebSocketChatClient(this, uri);
    }

    @Override
    public boolean supportsSecureConnection() {
        return false;
    }

    @SubscribeEvent
    public void handleClientCommand(ClientChatEvent e) {
        if (!e.getMessage().startsWith("/")) return;
        String[] split = e.getMessage().split(" ");
        String command = split[0].substring(1);
        ClientPlayerEntity player = Minecraft.getInstance().player;
        assert player != null;
        if (command.equals("cgtell") || command.equals("cgs") || command.equals("cg") || command.equals("cguild") || command.equals("reconnectinterchat") || command.equals("interchatconfig")) {
            try {
                player.connection.getCommandDispatcher().execute(e.getMessage().substring(1), player.getCommandSource());
            } catch (CommandSyntaxException ex) {
                player.sendMessage(new StringTextComponent(ex.getMessage()).applyTextStyles(TextFormatting.RED));
            }
            e.setMessage("");
            e.setCanceled(true);
        }
    }
}
