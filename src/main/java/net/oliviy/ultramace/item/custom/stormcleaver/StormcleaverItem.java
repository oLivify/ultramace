package net.oliviy.ultramace.item.custom.stormcleaver;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.*;
import java.util.function.Predicate;

public class StormcleaverItem extends SwordItem {
    public StormcleaverItem(Settings settings) {
        super(ModToolMaterials.MAGIC_INGOT, settings);
    }

    public static final Set<UUID> LIGHTNING_IMMUNE = new HashSet<>();
    private static final Map<UUID, Integer> lastPercent = new HashMap<>();
    private static final UUID REACH_UUID =
            UUID.fromString("2c2c1a6a-9c3b-4c7f-8b2c-111111111111");


    public static final long THUNDER_COOLDOWN = 300L;
    private static final String THUNDER_COOLDOWN_ID = "stormcleaver_thunder";



    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {

        ModItems.playCraftedSound(world, player);
        ModItems.giveDragonEgg(player);
        //ModItems.addEnchantment(world, stack, Enchantments.SHARPNESS, 10);
        super.onCraftByPlayer(stack, world, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof PlayerEntity player)) return;

        var attribute = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (attribute == null) return;

        // always remove first (prevents stacking bugs)
        attribute.removeModifier(
                new EntityAttributeModifier(
                        Identifier.of("stormcleaver_reach"),
                        3.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )
        );

        // apply only when held in main hand
        if (player.getMainHandStack().isOf(ModItems.STORMCLEAVER)) {

            attribute.addTemporaryModifier(
                    new EntityAttributeModifier(
                            Identifier.of("stormcleaver_reach"),
                            3.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        LIGHTNING_IMMUNE.add(attacker.getUuid());
        World world = attacker.getWorld();

        if (!world.isClient()) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);

            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(
                        target.getX(),
                        target.getY(),
                        target.getZ()
                );
                lightning.setCosmetic(true);

                world.spawnEntity(lightning);

            }
        }
        stormMaceHit(stack, target, attacker);
        return super.postHit(stack, target, attacker);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (CooldownManager.isOnCooldown(
                user,
                THUNDER_COOLDOWN_ID,
                THUNDER_COOLDOWN)) {

            return TypedActionResult.pass(user.getMainHandStack());
        }



        user.setCurrentHand(hand);

        return TypedActionResult.consume(user.getStackInHand(hand));
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {


        if (world.isClient()) return;
        if (!(user instanceof PlayerEntity player)) return;

        CooldownManager.startCooldown(player, THUNDER_COOLDOWN_ID, THUNDER_COOLDOWN);


        int usedTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;

        float charge = Math.min(1.0f, usedTicks / 60.0f);
        // 60 ticks = 3 seconds full charge

        double radius = 50.0 * charge; // optional scaling radius

        PlayerEntity caster = player;


        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity.isAlive() && entity != caster
        );

        float baseDamage = 6.0f;
        float damage = baseDamage + (charge * 18.0f);


        for (LivingEntity target : targets) {

            // 🌩️ spawn lightning
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);

            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(
                        target.getX(),
                        target.getY(),
                        target.getZ()
                );
                lightning.setCosmetic(true);

                world.spawnEntity(lightning);
            }

            // 💥 damage scales with charge
            target.damage(world.getDamageSources().playerAttack(player), damage);

            // 🌪️ knockback
            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();

            double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));

            target.addVelocity(
                    (dx / len) * (0.8 + charge),
                    0.4 + charge,
                    (dz / len) * (0.8 + charge)
            );

            target.velocityModified = true;
        }

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1, player.getZ(),
                    40,
                    3, 2, 3,
                    0.2
            );
        }



    }













    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {


        if (!(user instanceof PlayerEntity player)) return;
        if (world.isClient()) return;


        int usedTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;

        float charge = Math.min(1.0f, usedTicks / 60.0f);
        // 60 ticks = full charge (3 seconds)

        int percent = (int)(charge * 100);

        if (lastPercent.getOrDefault(player.getUuid(), -1) != percent) {
            lastPercent.put(player.getUuid(), percent);

            player.sendMessage(
                    Text.literal("§3⚡ Storm Charge: " + percent + "%§r"),
                    true
            );
        }


        if (world instanceof ServerWorld serverWorld && world.getTime() % 2 == 0) {

            double intensity = charge * 2.0;

            serverWorld.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    (int)(5 * intensity),
                    0.3, 0.4, 0.3,
                    0.05
            );
        }
    }


    // MACE ABILITIES BELOW

    public boolean stormMaceHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
