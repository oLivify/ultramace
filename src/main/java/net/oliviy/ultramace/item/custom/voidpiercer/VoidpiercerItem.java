package net.oliviy.ultramace.item.custom.voidpiercer;

import net.minecraft.datafixer.fix.ItemSpawnEggFix;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.*;

public class VoidpiercerItem extends SwordItem {


    public static final int BLINK_COOLDOWN = 120;
    private static final String BLINK_COOLDOWN_ID = "voidpiercer_blink";

    public static final int RIFT_COOLDOWN = 200;
    private static final String RIFT_COOLDOWN_ID = "voidpiercer_rift";

    public static final int CATACLYSM_COOLDOWN = 2400;
    private static final String CATACLYSM_COOLDOWN_ID = "voidpiercer_cataclysm";

    private final Map<UUID, Long> lastHit = new HashMap<>();

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();



    public VoidpiercerItem(Settings settings) {
        super(ModToolMaterials.MAGIC_INGOT, settings);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        super.onCraftByPlayer(stack, world, player);

        if (!world.isClient) {
            ModItems.giveDragonEgg(player);
            ModItems.playCraftedSound(world, player);
        }

    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        if (player.getOffHandStack().isOf(ModItems.VOIDPIERCER)) {
            if (SWORD_IN_OFFHAND.add(player.getUuid())) {
                if(world instanceof ServerWorld serverWorld) {
                    cataclysm(player, serverWorld);
                }

            }
        } else {
            SWORD_IN_OFFHAND.remove(player.getUuid());
        }


        if (selected) {
            if (player.isSneaking()) {
                blackHole(world, player);
            }

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
        long currentTick = world.getTime();

        Long lastHitTick = lastHit.get(id);

        if (lastHitTick != null) {

            long elapsed = currentTick - lastHitTick;

            // 40 ticks = 2 seconds
            if (elapsed <= 40) {

                target.damage(attacker.getDamageSources().magic(), 6.0f);

                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        200,
                        1
                ));

                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS,
                        200,
                        1
                ));
            }
        }

        lastHit.put(id, currentTick);


        if (attacker.isSneaking()) {

            if(attacker instanceof PlayerEntity user) {
                if (CooldownManager.isOnCooldown(user, RIFT_COOLDOWN_ID, RIFT_COOLDOWN)) {
                    return super.postHit(stack, target, attacker);
                }

                CooldownManager.startCooldown(user, RIFT_COOLDOWN_ID, RIFT_COOLDOWN);
            }



            riftSlash(attacker, stack, target, world);

        }


        Random random = new Random();

        if (random.nextInt(20) == 0) {
            itemLockTarget(target);

        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        ItemStack stack = user.getMainHandStack();


        if (CooldownManager.isOnCooldown(user, BLINK_COOLDOWN_ID, BLINK_COOLDOWN)) {
            return TypedActionResult.pass(stack);
        }

        CooldownManager.startCooldown(user, BLINK_COOLDOWN_ID, BLINK_COOLDOWN);


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


    private void itemLockTarget(LivingEntity entity) {
        if(entity instanceof PlayerEntity target) {
            Item mainHand = target.getMainHandStack().getItem();

            target.getItemCooldownManager().set(mainHand, 80);

            World world = target.getWorld();

            world.playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.BLOCK_ANVIL_PLACE,
                    target.getSoundCategory(),
                    1.0F,
                    1.0F
            );
        }
    }


    private void cataclysm(PlayerEntity player, ServerWorld serverWorld){

        if(player instanceof PlayerEntity user) {
            if (CooldownManager.isOnCooldown(user, CATACLYSM_COOLDOWN_ID, CATACLYSM_COOLDOWN)) {
                return;
            }

            CooldownManager.startCooldown(user, CATACLYSM_COOLDOWN_ID, CATACLYSM_COOLDOWN);
        }


        Vec3d look = player.getRotationVec(1.0F);

        Vec3d center = player.getPos().add(
                look.multiply(3)
        );

        CataclysmManager.spawn(
                serverWorld,
                player,
                center
        );
    }

}





