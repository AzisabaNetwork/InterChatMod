package net.azisaba.interchatmod.forge.mixin;

import net.azisaba.interchatmod.forge.Mod;
import net.azisaba.interchatmod.forge.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("generic.not_connected"));
            Mod.reconnect();
        }
    }

    @ModifyArg(method = "sendChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/CChatMessagePacket;<init>(Ljava/lang/String;)V"))
    public String modifyMessageBodyArg(String string) {
        return ModConfig.chatWithoutCommand && !string.startsWith("/") ? string.substring(1) : string;
    }
}
