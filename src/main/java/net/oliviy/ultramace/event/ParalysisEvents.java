package net.oliviy.ultramace.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.effects.ParalysisEffect;

public class ParalysisEvents {


    public static void register() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                if(!player.hasStatusEffect(ModEffects.PARALYSIS)) {

                    ParalysisEffect.removeParalysis(player);

                }

            }

        });

    }

}