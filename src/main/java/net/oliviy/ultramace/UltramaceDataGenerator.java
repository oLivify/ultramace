package net.oliviy.ultramace;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.oliviy.ultramace.datagen.ModItemTagProvider;
import net.oliviy.ultramace.datagen.ModModelProvider;
import net.oliviy.ultramace.datagen.ModRecipeProvider;

public class UltramaceDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();


		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModItemTagProvider::new);
	}
}
