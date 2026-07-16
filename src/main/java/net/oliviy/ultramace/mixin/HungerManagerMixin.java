package net.oliviy.ultramace.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.oliviy.ultramace.effects.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"
            )
    )
    private void ultramace$naturalRegen(PlayerEntity player, float amount) {

        if (player.hasStatusEffect(ModEffects.SOUL_SICKNESS)) {

            StatusEffectInstance effect =
                    player.getStatusEffect(ModEffects.SOUL_SICKNESS);

            int amplifier = effect == null ? 0 : effect.getAmplifier();

            // Heal slower depending on amplifier
            float multiplier = switch (amplifier) {
                case 0 -> 0.5F;
                case 1 -> 0.25F;
                default -> 0.0F;
            };

            player.heal(amount * multiplier);
            return;
        }

        player.heal(amount);
    }
}
