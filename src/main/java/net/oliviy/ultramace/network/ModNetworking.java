package net.oliviy.ultramace.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.oliviy.ultramace.network.payload.CooldownPayload;

public class ModNetworking {

    public static void register() {

        PayloadTypeRegistry.playS2C().register(
                CooldownPayload.ID,
                CooldownPayload.CODEC
        );

    }

}
