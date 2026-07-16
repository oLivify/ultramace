package net.oliviy.ultramace.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

public class SoulSicknessEffect extends StatusEffect {
    protected SoulSicknessEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }


    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {

        if (entity instanceof PlayerEntity player) {
            removeHelpfulEffects(player);


            HungerManager hunger = player.getHungerManager();

            hunger.setSaturationLevel(
                    Math.max(0, hunger.getSaturationLevel() - 0.6F * amplifier)
            );
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {

        return duration % 20 == 0;
    }


    private void removeHelpfulEffects(PlayerEntity player) {
        for (StatusEffectInstance effect : player.getStatusEffects()) {

            if (effect.getEffectType().value().getCategory() == StatusEffectCategory.BENEFICIAL) {
                player.removeStatusEffect(effect.getEffectType());
            }
        }
    }


}
