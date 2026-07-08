package net.oliviy.ultramace.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.oliviy.ultramace.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.FIRE_ORB, Models.GENERATED);
        itemModelGenerator.register(ModItems.FIRE_MACE, Models.HANDHELD_MACE);
        itemModelGenerator.register(ModItems.DAWNRNDER, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STORMCLEAVER, Models.HANDHELD);
        itemModelGenerator.register(ModItems.VOIDPIERCER, Models.HANDHELD);
        itemModelGenerator.register(ModItems.STARFALL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BLOODHARVESTER, Models.HANDHELD);
    }
}
