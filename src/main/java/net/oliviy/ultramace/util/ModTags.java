package net.oliviy.ultramace.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public class ModTags {
    public static class Blocks {

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Ultramace.MOD_ID, name));
        }
    }

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Ultramace.MOD_ID, name));
        }
    }

    public static class Entities {

        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Ultramace.MOD_ID, name)
            );
        }
    }
}
