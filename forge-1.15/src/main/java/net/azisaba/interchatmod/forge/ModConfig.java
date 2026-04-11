package net.azisaba.interchatmod.forge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.azisaba.interchatmod.common.util.Constants;
import net.azisaba.interchatmod.common.util.LazyValue;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ModConfig {
    private static final LazyValue<File> file =
            new LazyValue<>(LazyValue.Mode.NON_BLOCKING, () -> new File("./config/interchatmod.json"));

    public static String apiHost = "";
    public static String apiKey = "";
    public static boolean hideEverything = false;
    public static boolean chatWithoutCommand = false;

    public static @NotNull String getEffectiveApiHost() {
        return apiHost.isEmpty() ? Constants.DEFAULT_API_HOST : apiHost;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void prepareFile() throws IOException {
        File f = file.get();
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        f.createNewFile();
    }

    public static void save() {
        try {
            prepareFile();
            JsonObject object = new JsonObject();
            object.addProperty("apiHost", apiHost);
            object.addProperty("apiKey", apiKey);
            object.addProperty("hideEverything", hideEverything);
            object.addProperty("chatWithoutCommand", chatWithoutCommand);
            Files.write(file.get().toPath(), new Gson().toJson(object).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        try {
            if (!file.get().exists()) return;
            JsonObject object = new Gson().fromJson(new String(Files.readAllBytes(file.get().toPath()), StandardCharsets.UTF_8), JsonObject.class);
            if (object.has("apiHost")) {
                apiHost = object.get("apiHost").getAsString();
            }
            if (object.has("apiKey")) {
                apiKey = object.get("apiKey").getAsString();
            }
            if (object.has("hideEverything")) {
                hideEverything = object.get("hideEverything").getAsBoolean();
            }
            if (object.has("chatWithoutCommand")) {
                chatWithoutCommand = object.get("chatWithoutCommand").getAsBoolean();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
