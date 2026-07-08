package net.oliviy.ultramace.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.oliviy.ultramace.Ultramace;
import net.oliviy.ultramace.item.custom.FireMace;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;
import net.oliviy.ultramace.item.custom.stormcleaver.StormcleaverItem;
import net.oliviy.ultramace.item.custom.voidpiercer.VoidpiercerItem;

public class ModItems {

    public static final Item FIRE_ORB = registerItem("fire_orb", new Item(new Item.Settings().fireproof().rarity(Rarity.RARE)));
    public static final Item FIRE_MACE = registerItem("fire_mace", new FireMace(new Item.Settings().fireproof().maxDamage(750).rarity(Rarity.EPIC)
                                    .component(DataComponentTypes.TOOL, MaceItem.createToolComponent())
                                    .attributeModifiers(MaceItem.createAttributeModifiers())));

    public static final Item DAWNRNDER = Registry.register(
            Registries.ITEM,
            Identifier.of(Ultramace.MOD_ID, "dawnrender"),
            new DawnrenderItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.MAGIC_INGOT, 2, -2.3f)))
    );

    public static final Item STORMCLEAVER = Registry.register(
            Registries.ITEM,
            Identifier.of(Ultramace.MOD_ID, "stormcleaver"),
            new StormcleaverItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.MAGIC_INGOT, 3, -3.1f)))

    );

    public static final Item VOIDPIERCER = Registry.register(
            Registries.ITEM,
            Identifier.of(Ultramace.MOD_ID, "voidpiercer"),
            new VoidpiercerItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.MAGIC_INGOT, 1, -2.2f)))

    );

    public static final Item STARFALL = Registry.register(
            Registries.ITEM,
            Identifier.of(Ultramace.MOD_ID, "starfall"),
            new StarfallItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.MAGIC_INGOT, 0, -1.9f)))

    );

    public static final Item BLOODHARVESTER = Registry.register(
            Registries.ITEM,
            Identifier.of(Ultramace.MOD_ID, "bloodharvester"),
            new BloodharvesterItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .fireproof()
                    .attributeModifiers(SwordItem.createAttributeModifiers(
                            ModToolMaterials.MAGIC_INGOT, 3, -2.0f)))

    );








    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Ultramace.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Ultramace.LOGGER.info("Registering Mod Items for " + Ultramace.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(FIRE_ORB);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(FIRE_MACE);
            entries.add(DAWNRNDER);
            entries.add(STORMCLEAVER);
        });
    }

    public static void playCraftedSound(World world, PlayerEntity player) {
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_END_PORTAL_SPAWN,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }



    public static void giveDragonEgg(PlayerEntity player) {
        player.giveItemStack(new ItemStack(Items.DRAGON_EGG));
    }

    public static void addEnchantment(World world, ItemStack stack, RegistryKey<Enchantment> enchantment, int level) {

        RegistryEntry<Enchantment> entry = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(enchantment);

        stack.addEnchantment(entry, level);
    }

}
