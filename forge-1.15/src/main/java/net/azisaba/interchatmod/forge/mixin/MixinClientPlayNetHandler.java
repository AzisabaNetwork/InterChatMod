package net.azisaba.interchatmod.forge.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.azisaba.interchatmod.forge.Commands;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.play.server.SCommandListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class MixinClientPlayNetHandler {
    @Shadow private CommandDispatcher<ISuggestionProvider> commandDispatcher;

    @Inject(method = "handleCommandList", at = @At("TAIL"))
    public void handleCommandList(SCommandListPacket packetIn, CallbackInfo ci) {
        this.commandDispatcher.getRoot().addChild(Commands.builderGTell().build());
        this.commandDispatcher.getRoot().addChild(Commands.builderGS().build());
        this.commandDispatcher.getRoot().addChild(Commands.builderG().build());
        this.commandDispatcher.getRoot().addChild(Commands.builderReconnectInterChat().build());
        this.commandDispatcher.getRoot().addChild(Commands.builderGuild().build());
        this.commandDispatcher.getRoot().addChild(Commands.builderInterChatConfig().build());
    }
}
