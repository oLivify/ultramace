package net.oliviy.ultramace.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BleedParticle extends SpriteBillboardParticle {


    protected BleedParticle(ClientWorld clientWorld, double x, double y, double z,
                            SpriteProvider spriteProvider, double xD, double yD, double zD) {
        super(clientWorld, x, y, z, xD, yD, zD);

        this.maxAge = 60;
        this.setSpriteForAge(spriteProvider);

        this.red = 1f;
        this.green = 1f;
        this.blue = 1f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new BleedParticle(world, x, y, z, this.spriteProvider, velocityX, velocityY, velocityZ);
        }
    }

}
