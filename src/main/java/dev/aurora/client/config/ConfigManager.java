package dev.aurora.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.aurora.client.Aurora;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists module/setting state and HUD layout as JSON profiles under
 * {@code .minecraft/aurora/configs}. Supports multiple named profiles,
 * automatic timestamped backups, and atomic-ish writes.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File baseDir;
    private final File configDir;
    private final File backupDir;
    private String activeProfile = "default";

    public ConfigManager() {
        this.baseDir = new File(MinecraftClient.getInstance().runDirectory, "aurora");
        this.configDir = new File(baseDir, "configs");
        this.backupDir = new File(baseDir, "backups");
        configDir.mkdirs();
        backupDir.mkdirs();
    }

    public String getActiveProfile() { return activeProfile; }

    /** @return profile names discovered on disk (without extension). */
    public List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        File[] files = configDir.listFiles((d, n) -> n.endsWith(".json"));
        if (files != null) {
            for (File f : files) names.add(f.getName().replace(".json", ""));
        }
        if (!names.contains("default")) names.add("default");
        return names;
    }

    /** Switches profile and loads it (saving the current one first). */
    public void switchProfile(String name) {
        save();
        this.activeProfile = name;
        load();
    }

    /** Serialises all module state to the active profile file. */
    public void save() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        for (Module module : Aurora.INSTANCE.getModuleManager().getModules()) {
            JsonObject mObj = new JsonObject();
            mObj.addProperty("enabled", module.isEnabled());
            JsonObject settings = new JsonObject();
            for (Setting<?> s : module.getSettings()) {
                settings.add(s.getName(), s.toJson());
            }
            mObj.add("settings", settings);
            modules.add(module.getName(), mObj);
        }
        root.add("modules", modules);
        root.add("hud", Aurora.INSTANCE.getHudManager().toJson());
        root.addProperty("theme", Aurora.INSTANCE.getThemeManager().getActive().getName());

        File target = new File(configDir, activeProfile + ".json");
        backup(target);
        try (Writer w = new FileWriter(target)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            Aurora.LOGGER.error("Failed to save config '{}'", activeProfile, e);
        }
    }

    /** Loads the active profile file into module state, if present. */
    public void load() {
        File target = new File(configDir, activeProfile + ".json");
        if (!target.exists()) return;
        try (Reader r = new FileReader(target)) {
            JsonObject root = new JsonParser().parse(r).getAsJsonObject();
            if (root.has("theme")) {
                Aurora.INSTANCE.getThemeManager().setActive(root.get("theme").getAsString());
            }
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module module : Aurora.INSTANCE.getModuleManager().getModules()) {
                    if (!modules.has(module.getName())) continue;
                    JsonObject mObj = modules.getAsJsonObject(module.getName());
                    if (mObj.has("settings")) {
                        JsonObject settings = mObj.getAsJsonObject("settings");
                        for (Setting<?> s : module.getSettings()) {
                            if (settings.has(s.getName())) {
                                JsonElement el = settings.get(s.getName());
                                applySetting(s, el);
                            }
                        }
                    }
                    if (mObj.has("enabled") && mObj.get("enabled").getAsBoolean()) {
                        module.setEnabled(true);
                    }
                }
            }
            if (root.has("hud")) {
                Aurora.INSTANCE.getHudManager().fromJson(root.getAsJsonObject("hud"));
            }
        } catch (Exception e) {
            Aurora.LOGGER.error("Failed to load config '{}'", activeProfile, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void applySetting(Setting<?> s, JsonElement el) {
        ((Setting<Object>) s).fromJson(el);
    }

    /** Copies an existing config to the backup folder with a timestamp. */
    private void backup(File source) {
        if (!source.exists()) return;
        File dest = new File(backupDir, activeProfile + "_" + System.currentTimeMillis() + ".json");
        try (Reader r = new FileReader(source); Writer w = new FileWriter(dest)) {
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) != -1) w.write(buf, 0, n);
        } catch (IOException ignored) { /* backups are best-effort */ }
        pruneBackups(20);
    }

    /** Keeps only the most recent {@code keep} backups. */
    private void pruneBackups(int keep) {
        File[] files = backupDir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length <= keep) return;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        for (int i = 0; i < files.length - keep; i++) files[i].delete();
    }
}
