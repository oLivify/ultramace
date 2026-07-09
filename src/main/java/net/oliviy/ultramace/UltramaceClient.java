package net.oliviy.ultramace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.oliviy.ultramace.network.client.ModClientNetworking;
import net.oliviy.ultramace.particles.BleedParticle;
import net.oliviy.ultramace.particles.ModParticles;

public class UltramaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.BLEED,
                BleedParticle.Factory::new
        );
        ModClientNetworking.registerReceivers();

    }



}
