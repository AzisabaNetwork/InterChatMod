package net.azisaba.interchatmod.fabric;

import net.azisaba.interchatmod.common.config.ModConfigAccessor;
import net.azisaba.interchatmod.common.util.Constants;
import org.jetbrains.annotations.NotNull;

public class ModConfigAccessorImpl implements ModConfigAccessor {
    private final ModConfig config;

    public ModConfigAccessorImpl(ModConfig config) {
        this.config = config;
    }

    @Override
    public @NotNull String getEffectiveApiHost() {
        return config.apiHost.isEmpty() ? Constants.DEFAULT_API_HOST : config.apiHost;
    }

    @Override
    public @NotNull String getApiKey() {
        return config.apiKey;
    }

    @Override
    public void setApiKey(@NotNull String apiKey) {
        config.apiKey = apiKey;
    }
}
