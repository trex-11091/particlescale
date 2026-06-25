package de.mati.particlescale;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Schreibt beim Spawnen von Partikeln deren Typ-ID in den Chat – gedrosselt,
 * damit es nicht spammt. Hilft beim Herausfinden, welcher Partikel-Typ ein
 * bestimmter Effekt (z. B. eine Explosion) tatsächlich ist.
 */
public final class ParticleDebug {

    private static final Map<String, Long> LAST_LOGGED = new HashMap<>();
    private static final long THROTTLE_MS = 2500;

    private ParticleDebug() {}

    public static void onSpawn(Identifier id) {
        if (id == null || !ParticleScaleConfig.isDebugLog()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = LAST_LOGGED.get(id.toString());
        if (last != null && now - last < THROTTLE_MS) {
            return;
        }
        LAST_LOGGED.put(id.toString(), now);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        float scale = ParticleScaleConfig.getScale(id);
        String msg = "§b[ParticleScale] §f" + id + " §7(" + String.format("%.1fx", scale) + ")";
        mc.player.sendMessage(Text.literal(msg), false);
    }
}
