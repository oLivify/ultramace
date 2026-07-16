package net.oliviy.ultramace.item.custom.starfall;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.item.ItemAttributeManager;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.*;

public class StarfallItem extends SwordItem {

    // =========================
    // COOLDOWNS
    // =========================


    private static final Map<UUID, Integer> COMBO = new HashMap<>();
    private static final Map<UUID, Long> LAST_HIT = new HashMap<>();

    public static final long METEOR_COOLDOWN = 400L; //20 seconds
    private static final String METEOR_COOLDOWN_ID = "starfall_meteor";


    public static final long STARQUAKE_COOLDOWN = 400L; //30 seconds
    private static final String STARQUAKE_COOLDOWN_ID = "starfall_starquake";

    public static final long ULTIMATE_COOLDOWN = 1200L; //60 seconds
    private static final String ULTIMATE_COOLDOWN_ID = "starfall_ultimate";


    public static final Set<UUID> BREATH_IMMUNE = new HashSet<>();

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();




    // =========================
    // SIMPLE SCHEDULER (FIX)
    // =========================

    public static final List<ScheduledTask> TASKS = new ArrayList<>();

    public static class ScheduledTask {
        int ticks;
        Runnable action;

        ScheduledTask(int ticks, Runnable action) {
            this.ticks = ticks;
            this.action = action;
        }
    }

    public static void initScheduler() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            Iterator<ScheduledTask> it = TASKS.iterator();

            while (it.hasNext()) {
                ScheduledTask task = it.next();
                task.ticks--;

                if (task.ticks <= 0) {
                    task.action.run();
                    it.remove();
                }
            }
        });
    }

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new ScheduledTask(delayTicks, action));
    }

    // =========================
    // CONSTRUCTOR (FIXED)
    // =========================

    public StarfallItem(Settings settings) {
        super(ModToolMaterials.MAGIC_INGOT, settings);
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        ModItems.playCraftedSound(world, player);
        ModItems.giveDragonEgg(player);
        //ModItems.addEnchantment(world, stack, Enchantments.SHARPNESS, 10);

        super.onCraftByPlayer(stack, world, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        BREATH_IMMUNE.add(entity.getUuid());
        if(entity instanceof PlayerEntity player) {
            if (player.getOffHandStack().isOf(ModItems.STARFALL)) {
                if (SWORD_IN_OFFHAND.add(player.getUuid())) {
                    if (!(world instanceof ServerWorld serverWorld)) {
                        return;
                    }
                    ultimate(serverWorld, player);
                }
            } else {
                SWORD_IN_OFFHAND.remove(player.getUuid());
            }
        }



        super.inventoryTick(stack, world, entity, slot, selected);


    }


    // ==================================================
    // PASSIVE: FALLING STAR
    // ==================================================

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!(attacker instanceof PlayerEntity player)) return true;
        if (!(player.getWorld() instanceof ServerWorld world)) return true;

        UUID id = player.getUuid();

        long now = System.currentTimeMillis();
        long last = LAST_HIT.getOrDefault(id, 0L);

        if (now - last < 3000) {
            COMBO.put(id, Math.min(COMBO.getOrDefault(id, 0) + 2, 10));
        } else {
            COMBO.put(id, 0);
        }

        LAST_HIT.put(id, now);

        float bonus = COMBO.get(id);

        target.damage(world.getDamageSources().playerAttack(player), 6f + bonus);

        if (world.random.nextFloat() < 0.07f) {
            spawnHomingMeteor(world, target, attacker);
        }

        //falling trigger
        if (isFalling(player)) {
            cosmicDive(world, player);
        }

        if (player.isSneaking()) {
            starquake(world, player);
        }

        return true;
    }

    private void spawnHomingMeteor(ServerWorld world, LivingEntity target, LivingEntity owner) {

        Vec3d pos = target.getPos().add(0, 25, 0);

        DragonFireballEntity fireball = new DragonFireballEntity(
                world,
                target,
                pos
        );

        world.spawnEntity(fireball);

        Vec3d dir = target.getPos().subtract(pos).normalize().multiply(0.6);

        fireball.setVelocity(dir.x, dir.y, dir.z);


    }

    // ==================================================
    // METEOR SHOWER
    // ==================================================

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);

        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.pass(stack);
        }

        if (CooldownManager.isOnCooldown(
                user,
                METEOR_COOLDOWN_ID,
                METEOR_COOLDOWN)) {

            return TypedActionResult.pass(stack);
        }

        CooldownManager.startCooldown(user, METEOR_COOLDOWN_ID, METEOR_COOLDOWN);


        LivingEntity target = findEnemy(serverWorld, user);
        if (target == null) return TypedActionResult.pass(stack);

        Vec3d center = target.getPos();

        int meteors = 27;

        for (int i = 0; i < meteors; i++) {

            // RANDOM DELAY (this is what removes "all at once")
            int delay = (int) (Math.random() * 25); // 0–25 ticks stagger

            schedule(delay, () -> {

                // RANDOM CIRCULAR SPAWN (NOT SQUARE)
                double angle = Math.random() * Math.PI * 2;
                double radius = 8 + Math.random() * 18;

                double spawnX = center.x + Math.cos(angle) * radius;
                double spawnZ = center.z + Math.sin(angle) * radius;

                // random height variance
                double spawnY = center.y + 25 + Math.random() * 20;

                DragonFireballEntity meteor = new DragonFireballEntity(
                        EntityType.DRAGON_FIREBALL,
                        serverWorld
                );

                meteor.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0f, 0f);

                // RANDOMIZED FALL VECTOR (feels natural, not robotic)
                Vec3d velocity = new Vec3d(
                        (Math.random() - 0.5) * 0.2,
                        -0.6 - Math.random() * 0.4,
                        (Math.random() - 0.5) * 0.2
                );

                meteor.setVelocity(velocity);

                serverWorld.spawnEntity(meteor);
            });
        }

        return TypedActionResult.success(stack);
    }

    // ==================================================
    // STARQUAKE
    // ==================================================

    public void starquake(ServerWorld world, PlayerEntity player) {

        ItemStack stack = player.getMainHandStack();

        if (CooldownManager.isOnCooldown(
                player,
                STARQUAKE_COOLDOWN_ID,
                STARQUAKE_COOLDOWN)) {

            return;
        }

        CooldownManager.startCooldown(player, STARQUAKE_COOLDOWN_ID, STARQUAKE_COOLDOWN);

        Vec3d center = player.getPos();

        PlayerEntity caster = player;


        for (int wave = 1; wave <= 3; wave++) {

            int radius = wave * 4;

            schedule(wave * 20, () -> {

                for (LivingEntity e : world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(BlockPos.ofFloored(center)).expand(radius),
                        entity -> entity.isAlive() && entity != caster
                )) {

                    e.damage(world.getDamageSources().outOfWorld(), 10f);
                    e.addVelocity(0, 1.5, 0);
                }
            });
        }
    }

    // ==================================================
    // COSMIC DIVE
    // ==================================================

    public static void cosmicDive(ServerWorld world, PlayerEntity player) {

        Vec3d pos = player.getPos();


        double spawnX = pos.x;
        double spawnY = pos.y + 30;
        double spawnZ = pos.z;

        DragonFireballEntity meteor = new DragonFireballEntity(
                EntityType.DRAGON_FIREBALL,
                world
        );

        meteor.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0f, 0f);

        // IMPORTANT: aim directly at target (not just downward)
        Vec3d velocity = pos
                .subtract(new Vec3d(spawnX, spawnY, spawnZ))
                .normalize()
                .multiply(1.5); // higher speed = prevents early detonation bugs

        meteor.setVelocity(velocity);

        world.spawnEntity(meteor);

        for (LivingEntity e : world.getEntitiesByClass(
                LivingEntity.class,
                new Box(BlockPos.ofFloored(pos)).expand(8),
                Entity::isAlive)) {

            e.addVelocity(0, 2, 0);
        }
    }

    // ==================================================
    // ULTIMATE
    // ==================================================

    public void ultimate(ServerWorld world, PlayerEntity player) {

        if (CooldownManager.isOnCooldown(
                player,
                ULTIMATE_COOLDOWN_ID,
                ULTIMATE_COOLDOWN)) {

            return;
        }

        CooldownManager.startCooldown(player, ULTIMATE_COOLDOWN_ID, ULTIMATE_COOLDOWN);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 0));

        Vec3d pos = player.getPos();

        PlayerEntity caster = player;

        for (int t = 0; t < 200; t += 10) {
            schedule(t, () -> {
                for (LivingEntity e : world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(BlockPos.ofFloored(pos)).expand(10),
                        entity -> entity.isAlive() && entity != caster
                )) {

                    e.damage(world.getDamageSources().magic(), 8f);
                }
            });
        }

        ItemAttributeManager.applyUltimateEffects(player);
        ModItems.healPlayer(player, 20f);

        schedule(1800, () -> { // 30 seconds = 600 ticks
            ItemAttributeManager.removeUltimateEffects(player);

    });

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_BEACON_POWER_SELECT,
                SoundCategory.PLAYERS,
                2.0f,
                1.0f
        );

    }

    private boolean isFalling(PlayerEntity player) {
        return player.fallDistance > 2.5f && player.getVelocity().y < 0;
    }

    // ==================================================
    // FINDER
    // ==================================================

    private LivingEntity findEnemy(ServerWorld world, PlayerEntity player) {

        return world.getEntitiesByClass(
                LivingEntity.class,
                new Box(BlockPos.ofFloored(player.getPos())).expand(20),
                entity -> entity.isAlive() && entity != player
        ).stream().findFirst().orElse(null);
    }

}