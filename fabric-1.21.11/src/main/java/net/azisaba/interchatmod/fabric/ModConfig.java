package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.azisaba.interchatmod.common.util.Constants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final Path configDir = new File("config").toPath();
    private static final Path configFile = configDir.resolve("interchat.json");
    private static final Gson GSON = new Gson();
    public String apiHost = "";
    public String apiKey = "";
    public boolean hideEverything = false;
    public boolean chatWithoutCommand = false;

    public void load() {
        try {
            Path oldFile = configDir.resolve("interchat-config.json5");
            if (Files.exists(oldFile) && !Files.exists(configFile)) {
                Files.copy(oldFile, configFile);
            }
            if (Files.exists(configFile)) {
                JsonObject obj = GSON.fromJson(Files.readString(configFile), JsonObject.class);
                if (obj.has("apiHost")) {
                    apiHost = obj.get("apiHost").getAsString();
                }
                apiKey = obj.get("apiKey").getAsString();
                hideEverything = obj.get("hideEverything").getAsBoolean();
                chatWithoutCommand = obj.get("chatWithoutCommand").getAsBoolean();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull String getEffectiveApiHost() {
        return apiHost.isEmpty() ? Constants.DEFAULT_API_HOST : apiHost;
    }

    public void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("apiHost", apiHost);
        obj.addProperty("apiKey", apiKey);
        obj.addProperty("hideEverything", hideEverything);
        obj.addProperty("chatWithoutCommand", chatWithoutCommand);
        try {
            Files.writeString(configFile, GSON.toJson(obj));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull Screen createConfigScreen(@Nullable Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("InterChat"))
                .setSavingRunnable(() -> {
                    save();
                    Mod.reconnect();
                });
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        builder.getOrCreateCategory(Text.of("General"))
                .addEntry(entryBuilder.startStrField(Text.translatable("text.config.interchat-config.option.apiHost"), apiHost)
                        .setTooltip(Text.translatable("text.config.interchat-config.option.apiHost.tooltip"))
                        .setDefaultValue("")
                        .setSaveConsumer(apiHost -> this.apiHost = apiHost)
                        .build())
                .addEntry(entryBuilder.startStrField(Text.translatable("text.config.interchat-config.option.apiKey"), apiKey)
                        .setTooltip(Text.translatable("text.config.interchat-config.option.apiKey.tooltip"))
                        .setDefaultValue("")
                        .setSaveConsumer(apiKey -> this.apiKey = apiKey)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.config.interchat-config.option.hideEverything"), hideEverything)
                        .setTooltip(Text.translatable("text.config.interchat-config.option.hideEverything.tooltip"))
                        .setDefaultValue(false)
                        .setSaveConsumer(hideEverything -> this.hideEverything = hideEverything)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.config.interchat-config.option.chatWithoutCommand"), chatWithoutCommand)
                        .setDefaultValue(false)
                        .setSaveConsumer(chatWithoutCommand -> this.chatWithoutCommand = chatWithoutCommand)
                        .build());
        return builder.build();
    }
}
