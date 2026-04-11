package net.azisaba.interchatmod.fabric.mixin;

import net.azisaba.interchatmod.fabric.Mod;
import net.azisaba.interchatmod.fabric.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void maybeCancel(String content, CallbackInfo ci) {
        if (content.startsWith("/")) return;
        if (!ModConfig.chatWithoutCommand) {
            return;
        }
        if (content.startsWith("!")) {
            if (content.length() == 1) ci.cancel();
            return;
        }
        ci.cancel();
        try {
            Mod.client.sendMessageToGuild(null, content);
        } catch (WebsocketNotConnectedException e) {
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(new TranslatableText("generic.not_connected"), false);
            Mod.reconnect();
        }
    }

    @ModifyArg(method = "sendChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;<init>(Ljava/lang/String;)V"))
    public String modifyMessageBodyArg(String string) {
        return ModConfig.chatWithoutCommand && !string.startsWith("/") ? string.substring(1) : string;
    }
}
