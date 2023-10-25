package net.azisaba.interchatmod.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.azisaba.interchatmod.common.util.LazyValue;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ModConfig {
    private static final LazyValue<File> file =
            new LazyValue<>(LazyValue.Mode.NON_BLOCKING, () -> new File(FabricLoader.getInstance().getConfigDir().toFile(), "interchatmod.json"));

    public static String apiKey = "";
    public static boolean hideEverything = false;
    public static boolean chatWithoutCommand = false;

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
            object.addProperty("apiKey", apiKey);
            object.addProperty("hideEverything", hideEverything);
            object.addProperty("chatWithoutCommand", chatWithoutCommand);
            Files.writeString(file.get().toPath(), new Gson().toJson(object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        try {
            if (!file.get().exists()) return;
            JsonObject object = new Gson().fromJson(Files.readString(file.get().toPath()), JsonObject.class);
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
