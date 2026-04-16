package net.azisaba.interchatmod.forge;

import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import org.jetbrains.annotations.NotNull;

public class ModConfigAccessorImpl implements ModConfigAccessor {
    public static final ModConfigAccessorImpl INSTANCE = new ModConfigAccessorImpl();

    @Override
    public @NotNull String getEffectiveApiHost() {
        return ModConfig.getEffectiveApiHost();
    }

    @Override
    public @NotNull String getApiKey() {
        return ModConfig.apiKey;
    }

    @Override
    public void setApiKey(@NotNull String apiKey) {
        ModConfig.apiKey = apiKey;
    }
}
