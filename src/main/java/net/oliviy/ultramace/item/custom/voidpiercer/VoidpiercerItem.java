package net.oliviy.ultramace.item.custom.voidpiercer;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoidpiercerItem extends SwordItem {


    private static final int BLINK_COOLDOWN = 120; // 6s 120
    private static final int RIFT_COOLDOWN = 200;  // 10s 200

    private final Map<UUID, Long> lastBlinkUse = new HashMap<>();
    private final Map<UUID, Long> lastRiftUse = new HashMap<>();
    private final Map<UUID, Long> lastHit = new HashMap<>();

    public static final Map<UUID, Long> BLINK_COOLDOWN_MAP = new HashMap<>();
    public static final Map<UUID, Long> RIFT_COOLDOWN_MAP = new HashMap<>();

    private static final UUID HEALTH_UUID =
            UUID.fromString("c2f4c2d1-3d2a-4a8b-9d1a-1f7e6a9c1111");

    private static final UUID KB_UUID =
            UUID.fromString("a8c1f2b3-9d4e-4c7a-8d2b-2a9f1e3c2222");

    public VoidpiercerItem(Settings settings) {
        super(ModToolMaterials.MAGIC_INGOT, settings);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);

        if (!world.isClient) {
            ModItems.addEnchantment(world, stack, Enchantments.SHARPNESS, 10);
            ModItems.giveDragonEgg(player);
            ModItems.playCraftedSound(world, player);
        }

    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        var kbAttr = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);

        if (healthAttr == null || kbAttr == null) return;

        UUID healthId = HEALTH_UUID;
        UUID kbId = KB_UUID;

        // =========================
        // HEALTH BONUS (ONLY WHEN HELD)
        // =========================
        if (selected) {

            healthAttr.removeModifier(
                    new EntityAttributeModifier(
                            Identifier.of("voidpiercer_healthboost"),
                            20.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

            healthAttr.addTemporaryModifier(
                    new EntityAttributeModifier(
                            Identifier.of("voidpiercer_healthboost"),
                            6,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );



            // KB bonus only while sneaking + holding
            if (player.isSneaking()) {

                kbAttr.removeModifier(
                        new EntityAttributeModifier(
                                Identifier.of("voidpiercer_kb"),
                                .5,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                );

                kbAttr.addTemporaryModifier(
                        new EntityAttributeModifier(
                                Identifier.of("voidpiercer_kb"),
                                .5,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                );

                blackHole(world, player);
            }

        } else {

            // =========================
            // REMOVE WHEN NOT HELD
            // =========================

            healthAttr.removeModifier(
                    new EntityAttributeModifier(
                            Identifier.of("voidpiercer_healthboost"),
                            6,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );
            kbAttr.removeModifier(
                    new EntityAttributeModifier(
                            Identifier.of("voidpiercer_kb"),
                            .5,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        World world = attacker.getWorld();

        float baseDamage = 10.0f; // adjust for balance

        // First hit is normal (already applied by vanilla)
        // Second "void hit"
        target.damage(attacker.getDamageSources().magic(), baseDamage * 1.5f);

        // Passive tracking for Distance Collapse
        UUID id = target.getUuid();
        long now = System.currentTimeMillis();

        if (lastHit.containsKey(id)) {
            long diff = now - lastHit.get(id);

            if (diff <= 2000) {
                target.damage(attacker.getDamageSources().generic(), 6.0f); // bonus true-ish damage
                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, 200, 1
                ));
                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, 200, 1
                ));
            }
        }

        lastHit.put(id, now);


        if (attacker.isSneaking()) {

            UUID rID = attacker.getUuid();
            long rNOW = System.currentTimeMillis();

            if (lastRiftUse.containsKey(rID) &&
                    rNOW - lastRiftUse.get(rID) < RIFT_COOLDOWN * 50) {
                return super.postHit(stack, target, attacker);
            }

            lastRiftUse.put(rID, rNOW);
            RIFT_COOLDOWN_MAP.put(attacker.getUuid(), System.currentTimeMillis());

            riftSlash(attacker, stack, target, world);

        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));


        UUID id = user.getUuid();
        long now = System.currentTimeMillis();

        if (lastBlinkUse.containsKey(id) &&
                now - lastBlinkUse.get(id) < BLINK_COOLDOWN * 50) {
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        lastBlinkUse.put(id, now);
        BLINK_COOLDOWN_MAP.put(user.getUuid(), System.currentTimeMillis());


        VoidPearlEntity pearl = new VoidPearlEntity(world, user);

        // Spawn from the player's eyes
        pearl.setPosition(user.getX(), user.getEyeY() - 0.1, user.getZ());

        // Shoot where the player is looking
        pearl.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.5f, 0.0f);

        pearl.setCustomName(Text.literal("voidpiercer_pearl"));

        // Spawn it into the world
        world.spawnEntity(pearl);

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    public void riftSlash(LivingEntity attacker, ItemStack stack, LivingEntity target, World world) {
        Vec3d forward = attacker.getRotationVec(1.0F);
        Vec3d origin = attacker.getPos();

        Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0)).normalize();

        Box slashBox = new Box(origin, origin)
                .stretch(forward.multiply(8))
                .expand(1.5);

        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, slashBox, e -> e != attacker)) {

            // optional line filtering (keeps it a "slash")
            Vec3d toEntity = e.getPos().subtract(origin);
            double projection = toEntity.dotProduct(forward);

            if (projection < 0 || projection > 8) continue;
            if(attacker instanceof PlayerEntity player) {
                e.damage(attacker.getDamageSources().playerAttack(player), 10.0f);
            }


            // slight pull toward center line
            Vec3d pull = forward.multiply(0.2);
            e.addVelocity(pull.x, 0, pull.z);
        }
    }



    private void blackHole(World world, Entity entity) {

        if (!(entity instanceof PlayerEntity player)) return;

        if (!player.getMainHandStack().isOf(ModItems.VOIDPIERCER)) return;

        double radius = 12;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                e -> e != player && e.isAlive()
        );

        for (LivingEntity target : targets) {

            Vec3d direction = player.getPos().subtract(target.getPos());
            double distance = Math.max(0.1, direction.length());

            Vec3d pull = direction.normalize().multiply(0.2);
            // strength of pull

            // scale stronger when farther away (feels like vacuum)
            pull = pull.multiply(Math.min(1.0, distance / radius));

            target.addVelocity(pull.x, pull.y * 0.1, pull.z);

            target.velocityModified = true;
        }


        if (world.getTime() % 3 == 0) {
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        player.getX(),
                        player.getY() + 1,
                        player.getZ(),
                        2,
                        0.3, 0.3, 0.3,
                        0.01
                );
            }
        }

    }




}





