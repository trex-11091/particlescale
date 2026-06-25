package de.mati.particlescale.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.mati.particlescale.ParticleScaleConfig;
import de.mati.particlescale.duck.ScaledParticle;
import net.minecraft.client.particle.BillboardParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Multipliziert die beim Rendern verwendete Größe mit dem pro Typ
 * eingestellten Faktor. {@code BillboardParticle} ist die Basisklasse
 * fast aller sichtbaren Partikel (Flamme, Rauch, Funken, ...).
 */
@Mixin(BillboardParticle.class)
public abstract class BillboardParticleMixin {

    @ModifyReturnValue(method = "getSize(F)F", at = @At("RETURN"))
    private float particlescale$applyScale(float original) {
        ScaledParticle self = (ScaledParticle) (Object) this;
        return original * ParticleScaleConfig.getScale(self.particlescale$getTypeId());
    }
}
