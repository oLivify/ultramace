package net.oliviy.ultramace.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.particles.ModParticles;

public class BleedEffect extends StatusEffect {

    public BleedEffect(StatusEffectCategory category, int color) {
        super(category, color);

        addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of("ultramace", "bleed_slow"),
                -0.10,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {

        // Damage every second instead of every tick
        if (entity.age % 20 == 0) {
            entity.damage(
                    entity.getDamageSources().magic(),
                    1.0f + amplifier
            );

            if (entity.getWorld() instanceof ServerWorld world) {
                double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
                double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
                double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();

                world.spawnParticles(
                        ModParticles.BLEED,
                        x,
                        y,
                        z,
                        3,
                        0,
                        0,
                        0,
                        0
                );

            }



        }


        return true;
    }


    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }




}
