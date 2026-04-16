package net.azisaba.interchatmod.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ISuggestionProvider;

public class Commands {
    public static LiteralArgumentBuilder<ISuggestionProvider> builderInterChatConfig() {
        return LiteralArgumentBuilder.<ISuggestionProvider>literal("interchatconfig")
                .executes(ctx -> {
                    Minecraft.getInstance().enqueue(() ->
                            Minecraft.getInstance().displayGuiScreen(new ModConfigScreen(Minecraft.getInstance().currentScreen)));
                    return 0;
                });
    }
}
