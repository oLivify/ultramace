package net.oliviy.ultramace.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.oliviy.ultramace.item.ModItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FireMace extends MaceItem {
    public FireMace(Settings settings) {
        super(settings);
    }

    private static final Map<UUID, Integer> lastPercent = new HashMap<>();


    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {


        if (!world.isClient()) {
            ModItems.playCraftedSound(world, player);

        }
        super.onCraftByPlayer(stack, world, player);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 200;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        //System.out.println("Charging...");
        if (world.isClient()) return;
        if (!(user instanceof PlayerEntity player)) return;

        int usedTicks = getMaxUseTime(stack, user) - remainingUseTicks;

        // Full charge after 3 seconds (60 ticks)
        float charge = Math.min(1.0F, usedTicks / 60.0F);

        int percent = (int)(charge * 100);

        ServerWorld server = (ServerWorld) world;

        server.spawnParticles(
                ParticleTypes.FLAME,
                user.getX(),
                user.getY(),
                user.getZ(),
                4 + usedTicks / 5,
                0.2,
                0.2,
                0.2,
                0.02
        );

        world.playSound(
                null,
                user.getBlockPos(),
                SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );

        int interval = Math.max(2, 10 - usedTicks / 5);

        if (charge % interval == 0) {
            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F + charge * 0.02F
            );

        }


        if (lastPercent.getOrDefault(player.getUuid(), -1) != percent) {
            lastPercent.put(player.getUuid(), percent);

            player.sendMessage(
                    Text.literal("§6🔥 Fireball Charge: " + percent + "%§r"),
                    true
            );
        }

        if (charge > 20) {
            server.spawnParticles(
                    ParticleTypes.LAVA,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    2,
                    0.1,
                    0.1,
                    0.1,
                    0
            );


        }
    }




    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        //System.out.println("Released!");
        if (!(user instanceof PlayerEntity player)) return;
        if (world.isClient()) return;

        int usedTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        float charge = Math.min(1.0F, usedTicks / 60.0F);

        if (usedTicks < 10) return;

        ServerWorld server = (ServerWorld) world;

        FireballEntity fireball = new FireballEntity(
                world,
                player,
                player.getRotationVec(1.0F),
                1 + (int)(charge * 4) // Power 1–5
        );

        fireball.setPosition(
                player.getX(),
                player.getEyeY() - 0.2,
                player.getZ()
        );

        world.spawnEntity(fireball);

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );

        player.getItemCooldownManager().set(this, 40 + (int)(charge * 80));
    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.setOnFireFor(10);

        if(MaceItem.shouldDealAdditionalDamage(attacker)) {
            if (!target.getWorld().isClient()) {



            }
        }



        return super.postHit(stack, target, attacker);
    }



    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                5,
                0,
                true,
                false,
                false
        ));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        //fireDash(world, user, hand);

        ItemStack stack = user.getStackInHand(hand);

        user.setCurrentHand(hand);
        return super.use(world, user, hand);
    }

    private void fireDash(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {

            // direction player is facing
            Vec3d look = user.getRotationVec(1.0F);

            // DASH velocity
            user.addVelocity(
                    look.x * 1.6,
                    0.5,
                    look.z * 1.6
            );

            user.velocityModified = true;

            // cooldown so it can't spam
            user.getItemCooldownManager().set(this, 100);



                ((ServerWorld) world).spawnParticles(
                        ParticleTypes.FLAME,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        30,
                        0.2, 0.1, 0.2,
                        0.01
                );


            // optional sound
            world.playSound(
                    null,
                    user.getBlockPos(),
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
        }


    }



}
