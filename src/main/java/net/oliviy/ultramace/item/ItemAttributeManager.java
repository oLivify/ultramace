package net.oliviy.ultramace.item;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemAttributeManager {

    private static final Set<UUID> BLOODHARVESTER_PLAYERS = new HashSet<>();
    private static final Set<UUID> VOIDPIERCER_PLAYERS = new HashSet<>();
    private static final Set<UUID> STORM_PLAYERS = new HashSet<>();


    private static final Identifier BLOOD_SPEED =
            Identifier.of("blood_speed");

    private static final Identifier BLOOD_DAMAGE =
            Identifier.of("blood_damage");

    private static final Identifier BLOOD_HEALTH =
            Identifier.of("blood_health");

    private static final Identifier VOID_HEALTH =
            Identifier.of("void_health");

    private static final Identifier ULTIMATE_HEALTH =
            Identifier.of("ultimate_health");

    private static final Identifier STORM_REACH =
            Identifier.of("storm_reach");


    public static void tick(ServerPlayerEntity player) {

        handleBloodharvester(player);

        handleVoidpiercer(player);

        handleStormcleaver(player);

    }


    private static void handleBloodharvester(ServerPlayerEntity player) {

        if (hasItemInInventory(player, ModItems.BLOODHARVESTER)) {

            applyBloodEffects(player);

            BLOODHARVESTER_PLAYERS.add(player.getUuid());

        }
        else if (BLOODHARVESTER_PLAYERS.contains(player.getUuid())) {

            removeBloodEffects(player);

            BLOODHARVESTER_PLAYERS.remove(player.getUuid());
        }
    }

    private static void handleVoidpiercer(ServerPlayerEntity player) {

        if (hasItemInInventory(player, ModItems.VOIDPIERCER)) {

            applyVoidEffects(player);

            VOIDPIERCER_PLAYERS.add(player.getUuid());

        }
        else if (VOIDPIERCER_PLAYERS.contains(player.getUuid())) {

            removeVoidEffects(player);

            VOIDPIERCER_PLAYERS.remove(player.getUuid());
        }
    }

    private static void handleStormcleaver(ServerPlayerEntity player) {

        if (isHolding(player, ModItems.STORMCLEAVER)) {

            applyStormEffects(player);

            STORM_PLAYERS.add(player.getUuid());

        }
        else if (STORM_PLAYERS.contains(player.getUuid())) {

            removeStormEffects(player);

            STORM_PLAYERS.remove(player.getUuid());
        }
    }


    private static boolean isHolding(PlayerEntity player, Item item) {

        if(player.getMainHandStack().isOf(item)) {
            return true;
        }

        return false;
    }

    private static boolean hasItemInInventory(PlayerEntity player, Item item) {

        // Main inventory
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isOf(item)) {
                return true;
            }
        }

        // Hotbar is included in main, but this covers offhand
        if (player.getOffHandStack().isOf(item)) {
            return true;
        }

        return false;
    }

    private static void applyBloodEffects(PlayerEntity player) {

        int stacks = BloodharvesterItem.BLOOD_STACKS
                .getOrDefault(player.getUuid(), 0);


        var speed = player.getAttributeInstance(
                EntityAttributes.GENERIC_MOVEMENT_SPEED
        );

        var damage = player.getAttributeInstance(
                EntityAttributes.GENERIC_ATTACK_DAMAGE
        );

        var health = player.getAttributeInstance(
                EntityAttributes.GENERIC_MAX_HEALTH
        );


        if(speed != null) {
            speed.removeModifier(BLOOD_SPEED);

            speed.addTemporaryModifier(
                    new EntityAttributeModifier(
                            BLOOD_SPEED,
                            stacks * 0.005,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            );

        }


        if(damage != null) {
            damage.removeModifier(BLOOD_DAMAGE);

            damage.addTemporaryModifier(
                    new EntityAttributeModifier(
                            BLOOD_DAMAGE,
                            stacks * 0.2,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }


        if(health != null) {

            health.removeModifier(BLOOD_HEALTH);

            health.addTemporaryModifier(
                    new EntityAttributeModifier(
                            BLOOD_HEALTH,
                            stacks * 0.5,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }
    }

    private static void removeBloodEffects(PlayerEntity player) {

        var speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var damage = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var health = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);


        if (speed != null) {
            speed.removeModifier(BLOOD_SPEED);
        }


        if (damage != null) {
            damage.removeModifier(
                    BLOOD_DAMAGE
            );
        }


        if (health != null) {
            health.removeModifier(BLOOD_HEALTH);
        }
    }



    private static void applyVoidEffects(PlayerEntity player) {
        var health = player.getAttributeInstance(
                EntityAttributes.GENERIC_MAX_HEALTH
        );

        if(health != null) {

            health.removeModifier(VOID_HEALTH);

            health.addTemporaryModifier(
                    new EntityAttributeModifier(
                            VOID_HEALTH,
                            6,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }

    }

    private static void removeVoidEffects(PlayerEntity player) {

        var health = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);


        if (health != null) {
            health.removeModifier(VOID_HEALTH);
        }

    }


    public static void applyUltimateEffects(PlayerEntity player) {
        var health = player.getAttributeInstance(
                EntityAttributes.GENERIC_MAX_HEALTH
        );

        if(health != null) {

            health.removeModifier(ULTIMATE_HEALTH);

            health.addTemporaryModifier(
                    new EntityAttributeModifier(
                            ULTIMATE_HEALTH,
                            20,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }

    }

    public static void removeUltimateEffects(PlayerEntity player) {

        var health = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);


        if (health != null) {
            health.removeModifier(ULTIMATE_HEALTH);
        }

    }


    private static void applyStormEffects(PlayerEntity player) {
        var attribute = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);

        if(attribute != null) {

            attribute.removeModifier(STORM_REACH);

            attribute.addTemporaryModifier(
                    new EntityAttributeModifier(
                            STORM_REACH,
                            3,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }

    }

    public static void removeStormEffects(PlayerEntity player) {

        var attribute = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);


        if (attribute != null) {
            attribute.removeModifier(STORM_REACH);
        }

    }


}
