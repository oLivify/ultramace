package net.oliviy.ultramace.network.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.oliviy.ultramace.client.ClientCooldownManager;
import net.oliviy.ultramace.network.payload.CooldownPayload;

public class ModClientNetworking {

    public static void registerReceivers() {

        ClientPlayNetworking.registerGlobalReceiver(
                CooldownPayload.ID,
                (payload, context) -> {

                    ClientCooldownManager.setCooldown(
                            payload.abilityId(),
                            payload.endTick()
                    );

                }
        );

    }
}
