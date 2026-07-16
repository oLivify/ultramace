package net.oliviy.ultramace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.oliviy.ultramace.client.ClientParalysisEvents;
import net.oliviy.ultramace.entity.ModEntities;
import net.oliviy.ultramace.entity.SculkZombieRenderer;
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
        ClientParalysisEvents.register();


        EntityRendererRegistry.register(
                ModEntities.SUMMONED_ZOMBIE,
                SculkZombieRenderer::new
        );


    }



}
