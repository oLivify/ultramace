package net.oliviy.ultramace.effects;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.oliviy.ultramace.network.payload.ParalysisPayload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ParalysisEffect extends StatusEffect {

    private static final Set<UUID> PARALYZED_PLAYERS = new HashSet<>();

    public ParalysisEffect(StatusEffectCategory category, int color) {
        super(category, color);

    }


    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }


    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {

        // Stop movement
        entity.setVelocity(0, entity.getVelocity().y, 0);
        entity.velocityModified = true;

        if (entity instanceof ServerPlayerEntity player) {

            player.setVelocity(Vec3d.ZERO);
            player.velocityModified = true;
            player.setJumping(false);

            if (!PARALYZED_PLAYERS.contains(player.getUuid())) {

                PARALYZED_PLAYERS.add(player.getUuid());

                ServerPlayNetworking.send(
                        player,
                        new ParalysisPayload(true)
                );

            }

        }


        return true;
    }


    public static void removeParalysis(PlayerEntity player) {

        PARALYZED_PLAYERS.remove(player.getUuid());

        if(player instanceof ServerPlayerEntity serverPlayer){

            ServerPlayNetworking.send(
                    serverPlayer,
                    new ParalysisPayload(false)
            );

        }

    }

}
