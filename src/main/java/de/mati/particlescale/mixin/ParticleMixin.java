package de.mati.particlescale.mixin;

import de.mati.particlescale.duck.ScaledParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Hängt an jeden Partikel die Information, von welchem Typ er ist.
 * Wird von {@link ParticleManagerMixin} gesetzt und von
 * {@link BillboardParticleMixin} beim Rendern gelesen.
 */
@Mixin(Particle.class)
public abstract class ParticleMixin implements ScaledParticle {

    @Unique
    @Nullable
    private Identifier particlescale$typeId;

    @Override
    public void particlescale$setTypeId(@Nullable Identifier id) {
        this.particlescale$typeId = id;
    }

    @Override
    @Nullable
    public Identifier particlescale$getTypeId() {
        return this.particlescale$typeId;
    }
}
