package marc3d.mutils.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import meteordevelopment.meteorclient.MeteorClient;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataManager {
    private static final Gson GSON = new Gson();
    private static final File DATA_FILE = new File(MeteorClient.FOLDER, "mutils/player-data.json");
    private static final Map<String, PlayerData> playerData = new HashMap<>();

    public static class PlayerData {
        public String mainAccount;
        public List<String> altAccounts = new ArrayList<>();
        public String notes;
    }

    // Load from file
    public static void load() {
        if (!DATA_FILE.exists()) return;

        try {
            String json = Files.readString(DATA_FILE.toPath());
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loaded = GSON.fromJson(json, type);

            if (loaded != null) {
                playerData.clear();
                playerData.putAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save to file
    public static void save() {
        try {
            DATA_FILE.getParentFile().mkdirs();
            String json = GSON.toJson(playerData);
            Files.writeString(DATA_FILE.toPath(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get/Set player data
    public static PlayerData getData(String username) {
        return playerData.computeIfAbsent(username, k -> new PlayerData());
    }

    // Helper method to update and auto-save
    public static void setData(String username, PlayerData data) {
        playerData.put(username, data);
        save();
    }
}
