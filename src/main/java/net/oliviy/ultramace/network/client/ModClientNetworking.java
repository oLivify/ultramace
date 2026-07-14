package net.oliviy.ultramace.network.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.oliviy.ultramace.client.ClientCooldownManager;
import net.oliviy.ultramace.client.ClientParalysis;
import net.oliviy.ultramace.network.payload.CooldownPayload;
import net.oliviy.ultramace.network.payload.ParalysisPayload;

public class ModClientNetworking {

    public static void registerReceivers() {

        ClientPlayNetworking.registerGlobalReceiver(
                CooldownPayload.ID,
                (payload, context) -> {

                    if (payload.endTick() <= 0) {

                        ClientCooldownManager.removeCooldown(
                                payload.abilityId()
                        );

                    } else {

                        ClientCooldownManager.setCooldown(
                                payload.abilityId(),
                                payload.endTick()
                        );
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                ParalysisPayload.ID,
                (payload, context) -> {


                    ClientParalysis.setParalyzed(
                            payload.paralyzed()
                    );


                }
        );

    }
}
