package net.oliviy.ultramace.entity;


import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public class ModEntities {

    public static final EntityType<SummonedZombieEntity> SUMMONED_ZOMBIE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(Ultramace.MOD_ID, "summoned_zombie"),
                    EntityType.Builder.create(
                                    SummonedZombieEntity::new,
                                    SpawnGroup.MONSTER
                            )
                            .dimensions(
                                    0.6f,
                                    1.95f
                            )
                            .build()
            );




    public static void registerModEntities() {

        Ultramace.LOGGER.info(
                "Registering Mod Entities for " + Ultramace.MOD_ID
        );

        FabricDefaultAttributeRegistry.register(
                ModEntities.SUMMONED_ZOMBIE,
                ZombieEntity.createZombieAttributes()
        );

    }





}
