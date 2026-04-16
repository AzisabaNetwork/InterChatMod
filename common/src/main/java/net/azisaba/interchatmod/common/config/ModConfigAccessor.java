package net.azisaba.interchatmod.common.config;

import org.jetbrains.annotations.NotNull;

public interface ModConfigAccessor {
    @NotNull String getEffectiveApiHost();

    @NotNull String getApiKey();

    void setApiKey(@NotNull String apiKey);
}
