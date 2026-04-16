package net.azisaba.interchatmod.forge.mixin;

import net.azisaba.interchatmod.forge.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.TimerTask;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    public void onSetScreen(Screen screen, CallbackInfo ci) {
        Mod.instance.getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Mod.instance.trySwitch();
                } catch (Exception e) {
                    Mod.instance.getLogger().error("Failed to switch", e);
                }
            }
        }, 200);
    }
}
