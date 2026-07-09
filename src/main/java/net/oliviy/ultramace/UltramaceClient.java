package net.oliviy.ultramace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.oliviy.ultramace.item.custom.dawnrender.FreezeManager;
import net.oliviy.ultramace.particles.BleedParticle;
import net.oliviy.ultramace.particles.ModParticles;

public class UltramaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.BLEED,
                BleedParticle.Factory::new
        );

    }



}
