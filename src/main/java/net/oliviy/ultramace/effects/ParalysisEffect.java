package net.oliviy.ultramace.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ParalysisEffect extends StatusEffect {
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


        return true;
    }
}
