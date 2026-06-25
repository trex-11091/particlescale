package de.mati.particlescale.duck;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Wird per Mixin auf {@link net.minecraft.client.particle.Particle} aufgesetzt,
 * damit jeder Partikel weiß, von welchem Typ er ist. So kann die Render-Größe
 * pro Partikel-Typ skaliert werden.
 */
public interface ScaledParticle {
    void particlescale$setTypeId(@Nullable Identifier id);

    @Nullable
    Identifier particlescale$getTypeId();
}
