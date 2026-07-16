package net.oliviy.ultramace.item.custom.spectre_staff;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.entity.ModEntities;
import net.oliviy.ultramace.entity.SummonedZombieEntity;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;

import java.util.*;


public class SpectreStaffItem extends Item {

    public static final long SONIC_COOLDOWN = 400; // 20 seconds
    private static final String SONIC_COOLDOWN_ID = "spectre_sonic_boom";

    public static final long SUMMON_COOLDOWN = 800; // 20 seconds
    private static final String SUMMON_COOLDOWN_ID = "spectre_summon";

    public static final long SPREAD_COOLDOWN = 900; // 20 seconds
    private static final String SPREAD_COOLDOWN_ID = "spectre_spread";

    private static final Set<UUID> SWORD_IN_OFFHAND = new HashSet<>();

    private static final Map<UUID, Integer> HIT_COUNTER = new HashMap<>();





    public SpectreStaffItem(Settings settings) {
        super(settings);
    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(attacker instanceof PlayerEntity player) {
            commandMinions(player, target);
        }

        target.addStatusEffect(new StatusEffectInstance(ModEffects.SOUL_SICKNESS, 160, 1), attacker);
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(world.isClient()) return;
        if(!(entity instanceof PlayerEntity player)) return;
        if(!(world instanceof ServerWorld world1)) return;

        if (player.getOffHandStack().isOf(ModItems.SPECTRE_STAFF)) {
            if (SWORD_IN_OFFHAND.add(player.getUuid())) {
                spreadSculk(world1, player.getBlockPos(), player);
            }
        } else {
            SWORD_IN_OFFHAND.remove(player.getUuid());
        }

        applySculkPassive(player);


    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if (user.isSneaking()) {

            if (CooldownManager.isOnCooldown(
                    user,
                    SONIC_COOLDOWN_ID,
                    SONIC_COOLDOWN)) {

                return TypedActionResult.pass(user.getMainHandStack());
            }

            CooldownManager.startCooldown(user, SONIC_COOLDOWN_ID, SONIC_COOLDOWN);


            user.getWorld().playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.ENTITY_WARDEN_SONIC_CHARGE,
                    SoundCategory.PLAYERS,
                    1,
                    1
            );


            StarfallItem.schedule(40, () -> {
                useSonicBoom(user);
            });


            return TypedActionResult.success(user.getStackInHand(hand));

        } else {

            if (CooldownManager.isOnCooldown(
                    user,
                    SUMMON_COOLDOWN_ID,
                    SUMMON_COOLDOWN)) {

                return TypedActionResult.pass(user.getMainHandStack());
            }

            CooldownManager.startCooldown(user, SUMMON_COOLDOWN_ID, SUMMON_COOLDOWN);


            SummonedZombieEntity zombie = new SummonedZombieEntity(
                    ModEntities.SUMMONED_ZOMBIE,
                    world
            );

            zombie.refreshPositionAndAngles(
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    user.getYaw(),
                    0
            );

            zombie.setOwner(user.getUuid());
            zombie.equipStack(EquipmentSlot.OFFHAND, Items.TOTEM_OF_UNDYING.getDefaultStack());
            zombie.equipStack(EquipmentSlot.MAINHAND, Items.NETHERITE_SWORD.getDefaultStack());

            RegistryEntry<ArmorTrimMaterial> material =
                    world.getRegistryManager()
                            .get(RegistryKeys.TRIM_MATERIAL)
                            .getEntry(ArmorTrimMaterials.DIAMOND)
                            .orElseThrow();

            RegistryEntry<ArmorTrimPattern> vex =
                    world.getRegistryManager()
                            .get(RegistryKeys.TRIM_PATTERN)
                            .getEntry(ArmorTrimPatterns.VEX)
                            .orElseThrow();

            RegistryEntry<ArmorTrimPattern> rib =
                    world.getRegistryManager()
                            .get(RegistryKeys.TRIM_PATTERN)
                            .getEntry(ArmorTrimPatterns.RIB)
                            .orElseThrow();

            RegistryEntry<ArmorTrimPattern> flow =
                    world.getRegistryManager()
                            .get(RegistryKeys.TRIM_PATTERN)
                            .getEntry(ArmorTrimPatterns.FLOW)
                            .orElseThrow();

            RegistryEntry<ArmorTrimPattern> silence =
                    world.getRegistryManager()
                            .get(RegistryKeys.TRIM_PATTERN)
                            .getEntry(ArmorTrimPatterns.SILENCE)
                            .orElseThrow();

            RegistryEntry<Enchantment> vanishing =
                    world.getRegistryManager()
                            .get(RegistryKeys.ENCHANTMENT)
                            .getEntry(Enchantments.VANISHING_CURSE)
                            .orElseThrow();

            ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

            sword.addEnchantment(vanishing, 1);


            ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
            ItemStack chest = new ItemStack(Items.NETHERITE_CHESTPLATE);
            ItemStack leg = new ItemStack(Items.NETHERITE_LEGGINGS);
            ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

            ArmorTrim vexTrim = new ArmorTrim(material, vex);
            ArmorTrim ribTrim = new ArmorTrim(material, rib);
            ArmorTrim flowTrim = new ArmorTrim(material, flow);
            ArmorTrim silTrim = new ArmorTrim(material, silence);

            helmet.set(DataComponentTypes.TRIM, vexTrim);
            chest.set(DataComponentTypes.TRIM, silTrim);
            leg.set(DataComponentTypes.TRIM, ribTrim);
            boots.set(DataComponentTypes.TRIM, flowTrim);

            helmet.addEnchantment(vanishing, 1);
            chest.addEnchantment(vanishing, 1);
            leg.addEnchantment(vanishing, 1);
            boots.addEnchantment(vanishing, 1);


            zombie.equipStack(EquipmentSlot.HEAD, helmet);
            zombie.equipStack(EquipmentSlot.CHEST, chest);
            zombie.equipStack(EquipmentSlot.LEGS, leg);
            zombie.equipStack(EquipmentSlot.FEET, boots);



            zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100000, 2));
            zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100000, 1));
            zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100000, 2));

            world.spawnEntity(zombie);

        }

        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    private void useSonicBoom(PlayerEntity player) {

        World world = player.getWorld();

        if (world.isClient()) {
            return;
        }


        int hits = HIT_COUNTER.getOrDefault(player.getUuid(), 0) + 1;

        if (hits >= 5) {
            hits = 0;
            useSonicBurst(player);
        }

        HIT_COUNTER.put(player.getUuid(), hits);

        Vec3d start = player.getEyePos();

        Vec3d direction = player.getRotationVector();


        double range = 35.0;


        Vec3d end = start.add(
                direction.multiply(range)
        );


        // Find entities along the path
        Box box = player.getBoundingBox()
                .stretch(direction.multiply(range))
                .expand(1.0);

        PlayerEntity caster = player;


        List<LivingEntity> targets =
                world.getEntitiesByClass(
                        LivingEntity.class,
                        box,
                        entity ->
                                entity != caster &&
                                        entity.isAlive()
                );


        for (LivingEntity target : targets) {

            // Check if target is actually in front
            Vec3d toTarget =
                    target.getPos()
                            .subtract(start)
                            .normalize();


            double dot =
                    direction.dotProduct(toTarget);


            // Only hit things in the direction you look
            if (dot > 0.7) {

                target.addStatusEffect(new StatusEffectInstance(ModEffects.SOUL_SICKNESS, 120, 3));


                target.damage(
                        player.getDamageSources().sonicBoom(player),
                        15.0f
                );


                target.takeKnockback(
                        1.5,
                        -direction.x,
                        -direction.z
                );
            }
        }


        createParticles(
                world,
                start,
                direction,
                range
        );


        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }


    private void useSonicBurst(PlayerEntity player) {

        World world = player.getWorld();

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        double radius = 24.0;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity target : targets) {

            target.damage(
                    player.getDamageSources().sonicBoom(player),
                    15.0f
            );

            Vec3d knockback = target.getPos()
                    .subtract(player.getPos())
                    .normalize();

            target.takeKnockback(
                    2.0,
                    -knockback.x,
                    -knockback.z
            );
        }

        for (int i = 0; i < 8; i++) {

            double angle = Math.PI * 2 * i / 8;

            Vec3d direction = new Vec3d(
                    Math.cos(angle),
                    0,
                    Math.sin(angle)
            );

            createParticles(
                    world,
                    player.getEyePos(),
                    direction,
                    radius
            );
        }

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                SoundCategory.PLAYERS,
                2.0f,
                1.0f
        );
    }


    private void createParticles(World world, Vec3d start, Vec3d direction, double distance) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }


        for (int i = 0; i < distance * 5; i++) {

            Vec3d pos =
                    start.add(
                            direction.multiply(i / 5.0)
                    );


            serverWorld.spawnParticles(
                    ParticleTypes.SONIC_BOOM,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }


    private void applySculkPassive(PlayerEntity player) {

        if (!isStandingOnSculk(player)) {
            return;
        }

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE,
                40,
                2,
                true,
                false,
                true
        ));
    }



    private boolean isStandingOnSculk(PlayerEntity player) {

        World world = player.getWorld();

        BlockPos center = player.getBlockPos().down();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {

                BlockPos pos = center.add(x, 0, z);
                Block block = world.getBlockState(pos).getBlock();

                if (block == Blocks.SCULK
                        || block == Blocks.SCULK_VEIN
                        || block == Blocks.SCULK_CATALYST
                        || block == Blocks.SCULK_SENSOR
                        || block == Blocks.SCULK_SHRIEKER) {

                    return true;
                }
            }
        }

        return false;
    }

    private void spreadSculk(ServerWorld world, BlockPos center, PlayerEntity player) {


        if (CooldownManager.isOnCooldown(
                player,
                SPREAD_COOLDOWN_ID,
                SPREAD_COOLDOWN)) {

        }

        CooldownManager.startCooldown(player, SPREAD_COOLDOWN_ID, SPREAD_COOLDOWN);

        int radius = 8;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                // Makes the spread circular instead of square
                if (x * x + z * z > radius * radius) {
                    continue;
                }

                BlockPos surface = world.getTopPosition(
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                        center.add(x, 0, z)
                ).down();

                BlockState state = world.getBlockState(surface);

                if (canReplace(state)) {

                    float chance = world.random.nextFloat();

                    if (chance < 0.75f) {

                        world.setBlockState(
                                surface,
                                Blocks.SCULK.getDefaultState()
                        );

                    } else if (chance < 0.90f) {

                        world.setBlockState(
                                surface,
                                Blocks.SCULK_VEIN.getDefaultState()
                        );

                    } else if (chance < 0.97f) {

                        world.setBlockState(
                                surface,
                                Blocks.SCULK_CATALYST.getDefaultState()
                        );

                    } else {

                        world.setBlockState(
                                surface,
                                Blocks.SCULK_SHRIEKER.getDefaultState()
                        );

                    }
                }
            }
        }

        world.playSound(
                null,
                center,
                SoundEvents.BLOCK_SCULK_SPREAD,
                SoundCategory.BLOCKS,
                2.0f,
                1.0f
        );

    }

    private boolean canReplace(BlockState state) {

        Block block = state.getBlock();

        return block == Blocks.STONE
                || block == Blocks.DEEPSLATE
                || block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL
                || block == Blocks.MYCELIUM
                || block == Blocks.COBBLESTONE
                || block == Blocks.ANDESITE
                || block == Blocks.DIORITE
                || block == Blocks.GRANITE
                || block == Blocks.TUFF
                || block == Blocks.MOSS_BLOCK;
    }

    private void playSculkSound(ServerWorld world, BlockPos center) {
        world.playSound(
                null,
                center,
                SoundEvents.BLOCK_SCULK_CATALYST_BLOOM,
                SoundCategory.PLAYERS,
                1.5f,
                1.0f
        );
    }


    private boolean isOwnedBy(PlayerEntity player, MobEntity mob) {
        return mob instanceof Ownable ownable
                && Objects.requireNonNull(ownable.getOwner()).getUuid() != null
                && ownable.getOwner().getUuid().equals(player.getUuid());
    }


    public static void commandMinions(PlayerEntity player, LivingEntity target) {

        List<SummonedZombieEntity> minions =
                player.getWorld().getEntitiesByClass(
                        SummonedZombieEntity.class,
                        player.getBoundingBox().expand(50),
                        mob -> mob.getOwner() != null
                                && mob.getOwner().equals(player.getUuid())
                );


        for (SummonedZombieEntity minion : minions) {

            minion.setTarget(target);

        }
    }

}
