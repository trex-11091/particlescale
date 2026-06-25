package de.mati.particlescale.mixin;

import de.mati.particlescale.ParticleDebug;
import de.mati.particlescale.duck.ScaledParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Markiert jeden frisch erzeugten Partikel mit seinem Typ-Identifier,
 * damit die Render-Größe pro Typ angepasst werden kann.
 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(
            method = "createParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("RETURN")
    )
    private void particlescale$tagType(ParticleEffect parameters,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       CallbackInfoReturnable<Particle> cir) {
        Particle particle = cir.getReturnValue();
        if (particle == null) {
            return;
        }
        Identifier id = Registries.PARTICLE_TYPE.getId(parameters.getType());
        ((ScaledParticle) particle).particlescale$setTypeId(id);
        ParticleDebug.onSpawn(id);
    }
}
