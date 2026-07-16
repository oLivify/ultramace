package net.oliviy.ultramace.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;


import java.util.UUID;

public class SummonedZombieEntity extends ZombieEntity {

    private UUID owner;


    public SummonedZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }


    public void setOwner(UUID owner) {
        this.owner = owner;
    }


    public UUID getOwner() {
        return owner;
    }




    @Override
    public boolean tryAttack(Entity target) {

        // Don't attack owner
        if (target.getUuid().equals(owner)) {
            return false;
        }

        return super.tryAttack(target);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        if (owner != null) {
            nbt.putUuid(
                    "Owner",
                    owner
            );
        }
    }


    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.containsUuid("Owner")) {
            owner = nbt.getUuid("Owner");
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {

        if (target.getUuid().equals(owner)) {
            return false;
        }

        return super.canTarget(target);
    }

}