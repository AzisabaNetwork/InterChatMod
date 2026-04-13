package net.azisaba.interchatmod.forge.mixin;

import net.azisaba.interchatmod.forge.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ScreenShotHelper.class)
public class MixinScreenshotUtils {
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), method = "lambda$saveScreenshotRaw$2")
    private static void onSaveScreenshot(NativeImage nativeimage, File target, ScreenshotEvent event, Consumer<ITextComponent> messageConsumer, CallbackInfo ci) {
        UUID uuid = UUID.randomUUID();
        TextComponent text = new StringTextComponent("");
        text.appendText("[↑");
        text.appendSibling(new TranslationTextComponent("generic.upload"));
        text.appendText("↑]");
        text.applyTextStyle(style -> style
                .setColor(TextFormatting.AQUA)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("generic.upload.tooltip")))
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cguild upload_image " + uuid)));
        Minecraft.getInstance().execute(() -> messageConsumer.accept(text));
        Mod.images.put(uuid, target);
    }
}
