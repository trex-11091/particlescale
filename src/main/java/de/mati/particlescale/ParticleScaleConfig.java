package de.mati.particlescale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Speichert pro Partikel-Typ (Identifier als String) einen Skalierungsfaktor.
 * 1.0 = Originalgröße. Werte != 1.0 werden in {@code config/particlescale.json}
 * gespeichert; alles was 1.0 ist, wird weggelassen, damit die Datei klein bleibt.
 */
public final class ParticleScaleConfig {
    public static final float MIN_SCALE = 0.0f;
    public static final float MAX_SCALE = 10.0f;
    public static final float DEFAULT_SCALE = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Float>>() {}.getType();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("particlescale.json");

    private static final Map<String, Float> SCALES = new HashMap<>();

    // Nicht gespeichert: nur zur Laufzeit, um spawnende Partikel im Chat anzuzeigen.
    private static boolean debugLog = false;

    private ParticleScaleConfig() {}

    public static boolean isDebugLog() {
        return debugLog;
    }

    public static void setDebugLog(boolean value) {
        debugLog = value;
    }

    /** Aktueller Skalierungsfaktor eines Partikel-Typs (1.0 wenn nicht gesetzt). */
    public static float getScale(@Nullable Identifier id) {
        if (id == null) {
            return DEFAULT_SCALE;
        }
        return SCALES.getOrDefault(id.toString(), DEFAULT_SCALE);
    }

    /** Setzt den Faktor (geklemmt) und speichert sofort. */
    public static void setScale(Identifier id, float scale) {
        float clamped = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
        if (Math.abs(clamped - DEFAULT_SCALE) < 1.0e-4f) {
            SCALES.remove(id.toString());
        } else {
            SCALES.put(id.toString(), clamped);
        }
        save();
    }

    public static void resetAll() {
        SCALES.clear();
        save();
    }

    public static void load() {
        SCALES.clear();
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Map<String, Float> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                SCALES.putAll(loaded);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("[ParticleScale] Konnte Config nicht laden: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(SCALES, MAP_TYPE, writer);
            }
        } catch (IOException e) {
            System.err.println("[ParticleScale] Konnte Config nicht speichern: " + e.getMessage());
        }
    }
}
