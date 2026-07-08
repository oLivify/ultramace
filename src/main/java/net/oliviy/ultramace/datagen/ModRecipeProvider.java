package net.oliviy.ultramace.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.oliviy.ultramace.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.FIRE_ORB)
                .pattern("BNB")
                .pattern("NSN")
                .pattern("BNB")
                .input('B', Items.BLAZE_ROD)
                .input('N', Items.NETHERITE_INGOT)
                .input('S', Items.NETHER_STAR)
                .criterion(hasItem(Items.BLAZE_ROD), conditionsFromItem(Items.BLAZE_ROD))
                .group("mace")
                .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.FIRE_MACE)
               .pattern("RFR")
               .pattern("RMR")
               .pattern("RBR")
               .input('M', Items.MACE)
               .input('R', Items.BLAZE_ROD)
               .input('B', Items.BREEZE_ROD)
               .input('F', ModItems.FIRE_ORB)
               .criterion(hasItem(ModItems.FIRE_ORB), conditionsFromItem(ModItems.FIRE_ORB))
               .group("mace")
               .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.DAWNRNDER)
               .pattern(" WW")
               .pattern("WDW")
               .pattern("SW ")
               .input('S', Items.NETHERITE_SWORD)
               .input('D', Items.DRAGON_EGG)
               .input('W', Items.NETHER_STAR)
               .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
               .group("dawnrender")
               .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.STORMCLEAVER)
               .pattern("WWW")
               .pattern("WDW")
               .pattern(" A ")
               .input('A', Items.NETHERITE_AXE)
               .input('D', Items.DRAGON_EGG)
               .input('W', Items.NETHER_STAR)
               .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
               .group("stormcleaver")
               .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.VOIDPIERCER)
                .pattern(" EE")
                .pattern("WDE")
                .pattern("SW ")
                .input('S', Items.NETHERITE_SWORD)
                .input('D', Items.DRAGON_EGG)
                .input('W', Items.NETHER_STAR)
                .input('E', Items.ENDER_PEARL)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
                .group("voidpiercer")
                .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.STARFALL)
                .pattern(" EE")
                .pattern("WDE")
                .pattern("SW ")
                .input('S', Items.NETHERITE_SWORD)
                .input('D', Items.DRAGON_EGG)
                .input('W', Items.NETHER_STAR)
                .input('E', Items.ENCHANTED_GOLDEN_APPLE)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
                .group("starfall")
                .offerTo(exporter);

       ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.BLOODHARVESTER)
                .pattern(" WW")
                .pattern("WDW")
                .pattern("SW ")
                .input('S', Items.NETHERITE_SWORD)
                .input('D', Items.DRAGON_EGG)
                .input('W', Items.WITHER_SKELETON_SKULL)
                .criterion(hasItem(Items.NETHER_STAR), conditionsFromItem(Items.NETHER_STAR))
                .group("starfall")
                .offerTo(exporter);
    }
}
