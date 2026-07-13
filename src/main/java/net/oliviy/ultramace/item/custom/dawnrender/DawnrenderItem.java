package net.oliviy.ultramace.item.custom.dawnrender;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class DawnrenderItem extends SwordItem {

    public static final long DIVINE_CLEAVE_COOLDOWN = 20L * 60 * 2; // 2400 ticks
    private static final String DIVINE_CLEAVE_ID = "dawnrender_divine_cleave";

    public static final long DAWN_TOTEM_COOLDOWN = 20L * 90;         // 1800 ticks
    private static final String DAWN_TOTEM_ID = "dawnrender_dawn_totem";

    public static final String CELESTIAL_BINDING_ID = "dawnrender_celestial_binding";
    public static final long CELESTIAL_BINDING_COOLDOWN = 20 * 90; // 90 seconds

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();



    public DawnrenderItem(Settings settings) {
        super(ModToolMaterials.MAGIC_INGOT, settings);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);

        if (!world.isClient) {
            //ModItems.addEnchantment(world, stack, Enchantments.SHARPNESS, 10);
            ModItems.giveDragonEgg(player);
            ModItems.playCraftedSound(world, player);

        }

    }






    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        World world = attacker.getWorld();
        if (!world.isClient) {

            // Base radiant burst (2d8 equivalent simplified)
            target.damage(world.getDamageSources().magic(), 10.0f);

            // Divine Edge: aberration/fiend/undead bonus
            if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
                target.damage(world.getDamageSources().magic(), 15.0f);
            }


            bossKill(stack, target, attacker);

        }
        return super.postHit(stack, target, attacker);
    }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!(entity instanceof PlayerEntity player)) return;

        if (!world.isClient()) {

            if (player.getOffHandStack().isOf(ModItems.DAWNRNDER)) {
                if (SWORD_IN_OFFHAND.add(player.getUuid())) {
                    activateCelestialBinding(player);
                }
            } else {
                SWORD_IN_OFFHAND.remove(player.getUuid());
            }


            if(selected) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, true, false));
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient) {

            if (CooldownManager.isOnCooldown(
                    user,
                    DIVINE_CLEAVE_ID,
                    DIVINE_CLEAVE_COOLDOWN)) {

                return TypedActionResult.pass(stack);
            }

            CooldownManager.startCooldown(user, DIVINE_CLEAVE_ID, DIVINE_CLEAVE_COOLDOWN);

            // direction player is looking
            Vec3d lookDir = user.getRotationVec(1.0f);

            // center point 30 blocks in front
            Vec3d centerVec = user.getPos().add(lookDir.multiply(30));

            BlockPos center = BlockPos.ofFloored(centerVec);

            // area of effect (30 block radius)
            Box box = new Box(center).expand(30);

            PlayerEntity caster = user;

            for (LivingEntity e : world.getEntitiesByClass(
                    LivingEntity.class,
                    box,
                    entity -> entity.isAlive() && entity != caster
            )) {
                e.damage(world.getDamageSources().magic(), user.getHealth()*2);
            }
            serverWorld.spawnParticles(
                    ParticleTypes.END_ROD,
                    center.getX() + 0.5,
                    center.getY() + 1,
                    center.getZ() + 0.5,
                    200,
                    5, 10, 5,
                    0.5
            );
        }
        return TypedActionResult.success(stack);
    }

    public static boolean activateTotem(LivingEntity entity, DamageSource source, float amount) {


        if (!(entity instanceof PlayerEntity player)) return true;


        if (player.getMainHandStack().isOf(ModItems.DAWNRNDER)) {


            float healthAfter = player.getHealth() - amount;

            // ONLY trigger on lethal damage
            if (healthAfter <= 0.0f) {

                if (CooldownManager.isOnCooldown(
                        player,
                        DAWN_TOTEM_ID,
                        DAWN_TOTEM_COOLDOWN)) {

                    return true;
                }

                // prevent death
                player.setHealth(1.0f);

                // remove debuffs (totem-like feel)
                player.clearStatusEffects();

                // buffs
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.ABSORPTION,
                        20 * 60,
                        4
                ));

                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION,
                        20 * 10,
                        1
                ));

                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.RESISTANCE,
                        20 * 20,
                        1
                ));

                // visual totem effect
                player.getWorld().sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING);

                // Launch the player upward
                player.setVelocity(
                        player.getVelocity().x,
                        1.2, // Increase for a bigger launch
                        player.getVelocity().z
                );
                player.velocityModified = true;

                World world = player.getWorld();

                // Blast nearby mobs away
                double radius = 10.0;

                List<LivingEntity> nearby = world.getEntitiesByClass(
                        LivingEntity.class,
                        player.getBoundingBox().expand(radius),
                        blastEntity -> blastEntity != player
                );

                for (LivingEntity blastEntity : nearby) {

                    Vec3d knockback = blastEntity.getPos()
                            .subtract(player.getPos())
                            .normalize()
                            .multiply(2.5); // Horizontal strength

                    blastEntity.setVelocity(
                            knockback.x,
                            0.8,           // Vertical launch
                            knockback.z
                    );

                    blastEntity.velocityModified = true;

                }

                CooldownManager.startCooldown(player, DAWN_TOTEM_ID, DAWN_TOTEM_COOLDOWN);


                return false; // CANCEL death
            }
            return true;

        }
        return true;
}

    public static void activateCelestialBinding(PlayerEntity player) {

        World world = player.getWorld();

        if(world.isClient()) return;


        ServerWorld serverWorld = (ServerWorld) world;

        if (CooldownManager.isOnCooldown(
                player,
                CELESTIAL_BINDING_ID,
                CELESTIAL_BINDING_COOLDOWN)) {

            return;
        }

        CooldownManager.startCooldown(player, CELESTIAL_BINDING_ID, CELESTIAL_BINDING_COOLDOWN);

        PlayerEntity caster = player;


        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(15),
                entity -> entity.isAlive() && entity != caster
        );


        for(LivingEntity target : targets) {


            int duration = 20 * 3;

            target.addStatusEffect(
                    new StatusEffectInstance(
                            ModEffects.PARALYSIS,
                            duration,
                            0,
                            false,
                            true,
                            true
                    )
            );

            FreezeManager.freeze(target, duration);


            target.addStatusEffect(
                    new StatusEffectInstance(
                            StatusEffects.GLOWING,
                            duration,
                            0
                    )
            );

            target.damage(
                    world.getDamageSources().magic(),
                    10
            );
        }
        // Explosion of light
        serverWorld.spawnParticles(
                ParticleTypes.END_ROD,
                player.getX(),
                player.getY()+1,
                player.getZ(),
                250,
                4,
                1,
                4,
                0.1
        );
        player.playSound(
                SoundEvents.BLOCK_BEACON_POWER_SELECT,
                1,
                0.5f
        );
    }



public boolean bossKill(ItemStack stack, LivingEntity target, LivingEntity attacker) {


    if (target.getHealth() < 100.0f && (target instanceof EnderDragonEntity || target instanceof WitherEntity)) {
        target.damage(attacker.getWorld().getDamageSources().outOfWorld(),
                Float.MAX_VALUE);

        //target.kill();
    }

    return super.postHit(stack, target, attacker);
}

@Override
public boolean hasGlint(ItemStack stack) {
    return true;
}






}