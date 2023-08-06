package net.modfest.timelock.client.config;

import net.modfest.timelock.Timelock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TimelockConfig {

    public static final String FILENAME = "timelock.json";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enable = true;

    public static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILENAME);
    }

    public static TimelockConfig read() {
        try {
            return GSON.fromJson(Files.readString(file()), TimelockConfig.class);
        }
        catch (IOException e) {
            var config = new TimelockConfig();
            config.write();
            return config;
        }
    }

    public void write() {
        try {
            Files.writeString(file(), GSON.toJson(this));
        }
        catch (IOException e) {
            Timelock.LOGGER.error("Failed to write config file (timelock.json):", e);
        }
    }
}
