package net.oliviy.ultramace.item.custom.starfall;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.ModToolMaterials;

import java.util.*;

public class StarfallItem extends SwordItem {

    // =========================
    // COOLDOWNS
    // =========================

    public static final Map<UUID, Long> METEOR_COOLDOWN_MAP = new HashMap<>();
    public static final Map<UUID, Long> STARQUAKE_COOLDOWN_MAP = new HashMap<>();
    public static final Map<UUID, Long> ULTIMATE_COOLDOWN_MAP = new HashMap<>();

    private static final Map<UUID, Integer> COMBO = new HashMap<>();
    private static final Map<UUID, Long> LAST_HIT = new HashMap<>();

    private static final long METEOR_COOLDOWN = 20000;
    private static final long STARQUAKE_COOLDOWN = 30000;
    private static final long ULTIMATE_COOLDOWN = 60000;

    public static final Set<UUID> BREATH_IMMUNE = new HashSet<>();

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();

    private static final UUID ULTIMATE_HEALTH_UUID =
            UUID.fromString("8b5cbf88-5fd8-4c8f-90f5-6c7b0f4d4d7d");


    // =========================
    // SIMPLE SCHEDULER (FIX)
    // =========================

    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    private static class ScheduledTask {
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

    private static void schedule(int delayTicks, Runnable action) {
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
        ModItems.addEnchantment(world, stack, Enchantments.SHARPNESS, 10);

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
        System.out.println(6f + bonus);

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

        UUID id = user.getUuid();

        long now = System.currentTimeMillis();
        long last = METEOR_COOLDOWN_MAP.getOrDefault(id, 0L);

        if (now - last < METEOR_COOLDOWN) {
            return TypedActionResult.fail(stack);
        }

        METEOR_COOLDOWN_MAP.put(id, now);

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

        UUID id = player.getUuid();
        long now = System.currentTimeMillis();

        long last = STARQUAKE_COOLDOWN_MAP.getOrDefault(id, 0L);

        if (now - last < STARQUAKE_COOLDOWN) return;

        STARQUAKE_COOLDOWN_MAP.put(id, now);

        Vec3d center = player.getPos();

        for (int wave = 1; wave <= 3; wave++) {

            int radius = wave * 4;

            schedule(wave * 20, () -> {

                for (LivingEntity e : world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(BlockPos.ofFloored(center)).expand(radius),
                        entity -> entity.isAlive() && !(entity instanceof PlayerEntity)
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

        UUID id = player.getUuid();
        long now = System.currentTimeMillis();

        long last = ULTIMATE_COOLDOWN_MAP.getOrDefault(id, 0L);

        if (now - last < ULTIMATE_COOLDOWN) return;

        ULTIMATE_COOLDOWN_MAP.put(id, System.currentTimeMillis());

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 0));

        Vec3d pos = player.getPos();

        for (int t = 0; t < 200; t += 10) {
            schedule(t, () -> {
                for (LivingEntity e : world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(BlockPos.ofFloored(pos)).expand(10),
                        entity -> entity.isAlive() && !(entity instanceof PlayerEntity)
                )) {

                    e.damage(world.getDamageSources().magic(), 8f);
                }
            });
        }

        var healthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;

        // always remove first (prevents stacking bugs)
        healthAttribute.removeModifier(
                new EntityAttributeModifier(
                        Identifier.of("ultimate_health"),
                        20.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )
        );

        healthAttribute.addTemporaryModifier(
                new EntityAttributeModifier(
                        Identifier.of("ultimate_health"),
                        20.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                )
        );

        schedule(1800, () -> { // 30 seconds = 600 ticks


            // always remove first (prevents stacking bugs)
            healthAttribute.removeModifier(
                    new EntityAttributeModifier(
                            Identifier.of("ultimate_health"),
                            20.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );

    });

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
                        e -> e != player && e.isAlive())
                .stream().findFirst().orElse(null);
    }
}