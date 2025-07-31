package net.azisaba.interchatmod.fabric;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "interchatmod")
@Config(name = "interchat-config", wrapperName = "ModConfig")
public class ModConfigModel {
    public String apiKey = "";
    public boolean hideEverything = false;
    public boolean chatWithoutCommand = false;
}
