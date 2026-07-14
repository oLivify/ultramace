package net.oliviy.ultramace.cooldown;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.oliviy.ultramace.network.ModNetworking;
import net.oliviy.ultramace.network.payload.CooldownPayload;


public class CooldownManager {

    public static void startCooldown(PlayerEntity player, String abilityId, long cooldownTicks) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        CooldownState state = ModCooldowns.get(serverWorld);

        state.setCooldown(
                player.getUuid(),
                abilityId,
                serverWorld.getTime() + cooldownTicks
        );

        if (player instanceof ServerPlayerEntity serverPlayer) {

            ServerPlayNetworking.send(
                    serverPlayer,
                    new CooldownPayload(
                            abilityId,
                            serverWorld.getTime() + cooldownTicks
                    )
            );
        }


    }


    public static boolean isOnCooldown(PlayerEntity player, String abilityId, long cooldownTicks) {

        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        CooldownState state = ModCooldowns.get(serverWorld);

        return state.isOnCooldown(
                player.getUuid(),
                abilityId,
                serverWorld.getTime()
        );
    }


    public static long getRemainingCooldown(PlayerEntity player, String abilityId) {

        if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
            return 0;
        }

        CooldownState state = ModCooldowns.get(serverWorld);

        return state.getRemaining(
                player.getUuid(),
                abilityId,
                serverWorld.getTime()
        );
    }
}
