package net.azisaba.interchatmod.fabric.mixin;

import net.azisaba.interchatmod.fabric.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class MixinScreenshotRecorder {
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), method = "method_1661")
    private static void onSaveScreenshot(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
        UUID uuid = UUID.randomUUID();
        MutableText text = new LiteralText("");
        text.append("[↑");
        text.append(new TranslatableText("generic.upload"));
        text.append("↑]");
        text.styled(style -> style
                .withColor(Formatting.AQUA)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("generic.upload.tooltip")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cguild upload_image " + uuid)));
        MinecraftClient.getInstance().execute(() -> consumer.accept(text));
        Mod.images.put(uuid, file);
    }
}
