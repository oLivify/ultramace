package net.oliviy.ultramace.item.custom.bloodharvester;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


import java.util.*;
import java.util.function.Predicate;

public class BloodharvesterItem extends SwordItem {

    // =========================
    // Cooldowns
    // =========================

    public static final long BLOOD_RITE_COOLDOWN = 200; //10 sec
    private static final String BLOOD_RITE_COOLDOWN_ID = "bloodharvester_blood_rite";

    public static final long SWEEP_COOLDOWN = 120; // 6 seconds
    private static final String SWEEP_COOLDOWN_ID = "bloodharvester_sweep";


    public static final long DOMAIN_COOLDOWN = 400; // 20 seconds
    private static final String DOMAIN_COOLDOWN_ID = "bloodharvester_domain";




    // =========================
    // Blood Stacks
    // =========================

    public static final Map<UUID, Integer> BLOOD_STACKS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_BLOOD_DECAY = new HashMap<>();

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();


    public BloodharvesterItem(Settings settings) {
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
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(world.isClient()) return;
        if(!(entity instanceof PlayerEntity player)) return;


        int stacks = BLOOD_STACKS.getOrDefault(player.getUuid(), 0);


        bloodStackEffects(player);

        if (player.getOffHandStack().isOf(ModItems.BLOODHARVESTER)) {
            if (SWORD_IN_OFFHAND.add(player.getUuid())) {
                reaperDomain(world, player);
            }
        } else {
            SWORD_IN_OFFHAND.remove(player.getUuid());
        }

        if(selected){

            player.sendMessage(
                    Text.literal("§4Blood Stacks: " + stacks +"§r"),
                    true
            );

            if (player.getHealth() <= player.getMaxHealth() * 0.5f) {
                crimsonFeast(world,player);
            }




        }


        decayBloodStacks(player);

    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if(!(attacker instanceof PlayerEntity player))
            return super.postHit(stack,target,attacker);

        gainBloodStack(player);

        ModItems.healPlayer(player,.1f);

        applyBleed(target);

        if(player.isSneaking()){

            if (CooldownManager.isOnCooldown(
                    player,
                    SWEEP_COOLDOWN_ID,
                    SWEEP_COOLDOWN)) {

                return super.postHit(stack, target, attacker);
            }

            CooldownManager.startCooldown(player, SWEEP_COOLDOWN_ID);

            executionSweep(player,target);
        }

        bloodMaceStrike(stack, target, attacker);
        return super.postHit(stack,target,attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){

        if(world.isClient())
            return TypedActionResult.pass(user.getStackInHand(hand));

        if (CooldownManager.isOnCooldown(
                user,
                BLOOD_RITE_COOLDOWN_ID,
                BLOOD_RITE_COOLDOWN)) {

            return TypedActionResult.pass(user.getMainHandStack());
        }
        CooldownManager.startCooldown(user, BLOOD_RITE_COOLDOWN_ID);

        bloodRite(world,user);

        return TypedActionResult.success(user.getStackInHand(hand));
    }


    // ======================================================
    // Empty helper methods
    // ======================================================

    private void crimsonFeast(World world, PlayerEntity player) {

        double radius = 8.0;

        PlayerEntity caster = player;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity.isAlive() && entity != caster
        );

        for (LivingEntity target : targets) {

            applyBleed(target);

            // tiny damage tick (true-feeling bleed)
            target.damage(player.getDamageSources().magic(), 1.0f);

            // heal player per affected enemy (limited scaling)
            player.heal(0.1f);

            // particles
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.SOUL,
                        target.getX(),
                        target.getY() + 1,
                        target.getZ(),
                        1,
                        0.2, 0.2, 0.2,
                        0.01
                );
            }
        }
    }

    private void reaperDomain(World world, PlayerEntity player) {

        if (CooldownManager.isOnCooldown(
                player,
                DOMAIN_COOLDOWN_ID,
                DOMAIN_COOLDOWN)) {

            return;
        }

        CooldownManager.startCooldown(player, DOMAIN_COOLDOWN_ID);

        double radius = 10.0;

        PlayerEntity caster = player;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity.isAlive() && entity != caster
        );

        for (LivingEntity target : targets) {

            // constant suppression
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 1));

            // forced bleed tick
            applyBleed(target);

            // pull slightly toward player
            Vec3d pull = player.getPos().subtract(target.getPos()).normalize().multiply(0.15);
            target.addVelocity(pull.x, pull.y * 0.1, pull.z);

            target.velocityModified = true;

            // lifesteal aura
            player.heal(0.3f);
        }

        // DOMAIN VISUALS
        if (world instanceof ServerWorld serverWorld) {

            serverWorld.spawnParticles(
                    ParticleTypes.SMOKE,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    10,
                    2.5, 1.0, 2.5,
                    0.02
            );

            serverWorld.spawnParticles(
                    ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    25,
                    2.0, 1.0, 2.0,
                    0.01
            );
        }

        // reward stacks for staying in domain
        gainBloodStack(player);
    }

    private void bloodRite(World world, PlayerEntity player) {

        double radius = 6.0;

        // COST: player sacrifices health
        player.damage(player.getDamageSources().magic(), 8.0f);

        PlayerEntity caster = player;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity.isAlive() && entity != caster
        );

        int hitCount = 0;

        for (LivingEntity target : targets) {

            target.damage(player.getDamageSources().magic(), 10.0f);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));

            applyBleed(target);

            player.heal(1.0f);
            hitCount++;

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.SOUL,
                        target.getX(),
                        target.getY() + 1,
                        target.getZ(),
                        3,
                        0.3, 0.3, 0.3,
                        0.01
                );
            }
        }

        // bonus reward if good hit
        if (hitCount >= 3) {
            player.heal(6.0f);
            gainBloodStack(player);
            gainBloodStack(player);
        }

        // explosion visuals
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.LARGE_SMOKE,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    20,
                    1.0, 0.5, 1.0,
                    0.02
            );
        }
    }

    private void executionSweep(PlayerEntity player, LivingEntity ignoredTarget) {

        World world = player.getWorld();

        Vec3d forward = player.getRotationVec(1.0F);
        Vec3d origin = player.getPos();

        Box box = new Box(origin, origin).stretch(forward.multiply(5)).expand(2.5);

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != player && e.isAlive()
        );

        for (LivingEntity target : targets) {

            Vec3d toTarget = target.getPos().subtract(origin);
            double projection = toTarget.dotProduct(forward);

            if (projection < 0 || projection > 5) continue;

            float damage = 8.0f;

            if (target.getHealth() <= target.getMaxHealth() * 0.25f) {
                damage = 999.0f;
                gainBloodStack(player);
                gainBloodStack(player);
            }

            target.damage(player.getDamageSources().playerAttack(player), damage);

            applyBleed(target);

            // pull into slash
            Vec3d pull = forward.multiply(0.3);
            target.addVelocity(pull.x, 0.1, pull.z);

            target.velocityModified = true;

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.CRIMSON_SPORE,
                        target.getX(),
                        target.getY() + 1,
                        target.getZ(),
                        2,
                        0.2, 0.2, 0.2,
                        0.01
                );
            }
        }
    }

    private void applyBleed(LivingEntity target) {
        target.addStatusEffect(
                new StatusEffectInstance(
                        ModEffects.BLEEDING,
                        100,
                        0
                )
        );
    }

    private void gainBloodStack(PlayerEntity player){

        UUID id=player.getUuid();

        int stacks=BLOOD_STACKS.getOrDefault(id,0);

        if(stacks<20){
            BLOOD_STACKS.put(id,stacks+1);
        }


    }

    private void decayBloodStacks(PlayerEntity player) {

        UUID id = player.getUuid();

        if (!NEXT_BLOOD_DECAY.containsKey(id)) {
            return;
        }

        if(player.age >= NEXT_BLOOD_DECAY.getOrDefault(id, Long.MAX_VALUE)) {

            int stacks = BLOOD_STACKS.getOrDefault(id, 0);

            if(stacks > 0) {
                BLOOD_STACKS.put(id, stacks - 1);

                NEXT_BLOOD_DECAY.put(
                        id,
                        player.age + 50L
                );
            }
        }

    }



    private void bloodStackEffects(PlayerEntity player) {

        int stacks = BLOOD_STACKS.getOrDefault(player.getUuid(), 0);

        if (!(player.getWorld() instanceof ServerWorld world)) return;

        if (stacks > 10) {

            world.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(),
                    player.getY() + 0.05,
                    player.getZ(),
                    4,
                    0.5, 0.05, 0.5,
                    0.01
            );
        }
    }

    public static void addBloodStacks(PlayerEntity player, int amount) {

        UUID id = player.getUuid();

        int current = BLOOD_STACKS.getOrDefault(id, 0);

        BLOOD_STACKS.put(
                id,
                Math.min(current + amount, 20)
        );

        NEXT_BLOOD_DECAY.put(
                id,
                player.age + 50L
        );
    }


    // MACE ABILITIES BELOW

    public boolean bloodMaceStrike(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayerEntity serverPlayerEntity && shouldDealAdditionalDamage(serverPlayerEntity)) {
            ServerWorld serverWorld = (ServerWorld)attacker.getWorld();
            if (serverPlayerEntity.shouldIgnoreFallDamageFromCurrentExplosion() && serverPlayerEntity.currentExplosionImpactPos != null) {
                if (serverPlayerEntity.currentExplosionImpactPos.y > serverPlayerEntity.getPos().y) {
                    serverPlayerEntity.currentExplosionImpactPos = serverPlayerEntity.getPos();
                }
            } else {
                serverPlayerEntity.currentExplosionImpactPos = serverPlayerEntity.getPos();
            }

            serverPlayerEntity.setIgnoreFallDamageFromCurrentExplosion(true);
            serverPlayerEntity.setVelocity(serverPlayerEntity.getVelocity().withAxis(Direction.Axis.Y, 0.01F));
            serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
            if (target.isOnGround()) {
                serverPlayerEntity.setSpawnExtraParticlesOnFall(true);
                SoundEvent soundEvent = serverPlayerEntity.fallDistance > 5.0F ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND;
                serverWorld.playSound(
                        null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), soundEvent, serverPlayerEntity.getSoundCategory(), 1.0F, 1.0F
                );
            } else {
                serverWorld.playSound(
                        null,
                        serverPlayerEntity.getX(),
                        serverPlayerEntity.getY(),
                        serverPlayerEntity.getZ(),
                        SoundEvents.ITEM_MACE_SMASH_AIR,
                        serverPlayerEntity.getSoundCategory(),
                        1.0F,
                        1.0F
                );
            }

            knockbackNearbyEntities(serverWorld, serverPlayerEntity, target);
        }

        return true;
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        if (shouldDealAdditionalDamage(attacker)) {
            attacker.onLanding();
        }
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        if (damageSource.getSource() instanceof LivingEntity livingEntity) {
            if (!shouldDealAdditionalDamage(livingEntity)) {
                return 0.0F;
            }

            float f = 3.0F;
            float g = 8.0F;
            float h = livingEntity.fallDistance;
            float i;
            if (h <= 3.0F) {
                i = 4.0F * h;
            } else if (h <= 8.0F) {
                i = 12.0F + 2.0F * (h - 3.0F);
            } else {
                i = 22.0F + h - 8.0F;
            }

            return livingEntity.getWorld() instanceof ServerWorld serverWorld
                    ? i + EnchantmentHelper.getSmashDamagePerFallenBlock(serverWorld, livingEntity.getWeaponStack(), target, damageSource, 0.0F) * h
                    : i;
        } else {
            return 0.0F;
        }
    }

    private static void knockbackNearbyEntities(World world, PlayerEntity player, Entity attacked) {
        world.syncWorldEvent(WorldEvents.SMASH_ATTACK, attacked.getSteppingPos(), 750);
        world.getEntitiesByClass(LivingEntity.class, attacked.getBoundingBox().expand(3.5), getKnockbackPredicate(player, attacked)).forEach(entity -> {
            Vec3d vec3d = entity.getPos().subtract(attacked.getPos());
            double d = getKnockback(player, entity, vec3d);
            Vec3d vec3d2 = vec3d.normalize().multiply(d);
            if (d > 0.0) {
                entity.addVelocity(vec3d2.x, 0.7F, vec3d2.z);
                if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                    serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                }
            }
        });
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(PlayerEntity player, Entity attacked) {
        return entity -> {
            boolean bl = !entity.isSpectator();
            boolean bl2 = entity != player && entity != attacked;
            boolean bl3 = !player.isTeammate(entity);
            boolean bl4 = !(entity instanceof TameableEntity tameableEntity && tameableEntity.isTamed() && player.getUuid().equals(tameableEntity.getOwnerUuid()));
            boolean bl5 = !(entity instanceof ArmorStandEntity armorStandEntity && armorStandEntity.isMarker());
            boolean bl6 = attacked.squaredDistanceTo(entity) <= Math.pow(3.5, 2.0);
            return bl && bl2 && bl3 && bl4 && bl5 && bl6;
        };
    }

    private static double getKnockback(PlayerEntity player, LivingEntity attacked, Vec3d distance) {
        return (3.5 - distance.length())
                * 0.7F
                * (player.fallDistance > 5.0F ? 2 : 1)
                * (1.0 - attacked.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
    }

    public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
        return attacker.fallDistance > 1.5F && !attacker.isFallFlying();
    }

}
