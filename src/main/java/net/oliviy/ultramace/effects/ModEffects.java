package net.oliviy.ultramace.effects;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public class ModEffects {

    public static final RegistryEntry<StatusEffect> BLEED =
            Registry.registerReference(
                    Registries.STATUS_EFFECT,
                    Identifier.of(Ultramace.MOD_ID, "bleed"),
                    new BleedEffect()
            );




    private static RegistryEntry<StatusEffect> registerStatusEffect(String name, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(Ultramace.MOD_ID, name), statusEffect);
    }

    public static void registerEffects() {
        Ultramace.LOGGER.info("Registering Mod Effects for " + Ultramace.MOD_ID);
    }
}
