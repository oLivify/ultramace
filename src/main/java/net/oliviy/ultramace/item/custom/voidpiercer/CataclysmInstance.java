package net.oliviy.ultramace.item.custom.voidpiercer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CataclysmInstance {

    private final ServerWorld world;
    private final PlayerEntity caster;
    private final Vec3d center;

    private int age = 0;


    public CataclysmInstance(ServerWorld world, PlayerEntity caster, Vec3d center) {
        this.world = world;
        this.caster = caster;
        this.center = center;

        world.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.BLOCK_END_GATEWAY_SPAWN,
                SoundCategory.PLAYERS,
                2f,
                0.8f
        );
    }

    public boolean tick() {

        age++;

        if (age <= 40) {
            phaseOne();
        }
        else if (age <= 120) {
            phaseTwo();
        }
        else if (age == 140) {
            phaseThree();
        }

        return age > 140;
    }

    private List<LivingEntity> getTargets() {

        return world.getEntitiesByClass(
                LivingEntity.class,
                new Box(center, center).expand(15),
                e -> e != caster && e.isAlive()
        );
    }

    private void phaseOne() {

        world.spawnParticles(
                ParticleTypes.PORTAL,
                center.x,
                center.y,
                center.z,
                20,
                2,
                2,
                2,
                0.2
        );

        for (LivingEntity entity : getTargets()) {

            Vec3d pull = center.subtract(entity.getPos())
                    .normalize()
                    .multiply(0.35);

            entity.addVelocity(
                    pull.x,
                    pull.y * 0.4,
                    pull.z
            );
        }
    }

    private void phaseTwo() {

        world.spawnParticles(
                ParticleTypes.REVERSE_PORTAL,
                center.x,
                center.y,
                center.z,
                40,
                3,
                3,
                3,
                0.1
        );

        if (age % 20 == 0) {

            for (LivingEntity entity : getTargets()) {

                entity.damage(
                        world.getDamageSources().magic(),
                        8f
                );

                Vec3d pull = center.subtract(entity.getPos())
                        .normalize()
                        .multiply(0.55);

                entity.addVelocity(
                        pull.x,
                        pull.y * 0.5,
                        pull.z
                );
            }
        }
    }

    private void phaseThree() {
        world.spawnParticles(
                ParticleTypes.DRAGON_BREATH,
                center.x,
                center.y,
                center.z,
                300,
                5,
                5,
                5,
                0.2
        );

        world.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.ENTITY_ENDER_DRAGON_DEATH,
                SoundCategory.PLAYERS,
                2f,
                1f
        );

        for (LivingEntity entity : getTargets()) {

            Vec3d knock = entity.getPos()
                    .subtract(center)
                    .normalize()
                    .multiply(2);

            entity.setVelocity(
                    knock.x,
                    1.2,
                    knock.z
            );

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 150, 4, false, false, false));
            lockInventory(entity);
        }
    }

    private void lockInventory(LivingEntity entity) {
        if(entity instanceof PlayerEntity player) {
            // Main inventory + hotbar
            for (ItemStack stack : player.getInventory().main) {
                player.getItemCooldownManager().set(stack.getItem(), 150);
            }
        }
    }




}

