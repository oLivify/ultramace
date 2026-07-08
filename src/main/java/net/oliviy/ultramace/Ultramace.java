package net.oliviy.ultramace;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.hud.ModHud;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;
import net.oliviy.ultramace.item.custom.stormcleaver.StormcleaverItem;
import net.oliviy.ultramace.particles.ModParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ultramace implements ModInitializer {
	public static final String MOD_ID = "ultramace";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		ModItems.registerModItems();
		ModEffects.registerEffects();
		ModParticles.registerParticles();
		register();
		checkTotem();
		HudRenderCallback.EVENT.register(new ModHud());
		StarfallItem.initScheduler();

	}

	public static void register() {


		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

			if (entity instanceof PlayerEntity) {

				if (source.isOf(DamageTypes.LIGHTNING_BOLT) && StormcleaverItem.LIGHTNING_IMMUNE.contains(entity.getUuid())) {
					return false; // CANCEL DAMAGE
				}
			}

			return true;
		});



		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

			if (entity instanceof PlayerEntity) {

			if (source.isOf(DamageTypes.INDIRECT_MAGIC) && StarfallItem.BREATH_IMMUNE.contains(entity.getUuid())) {
					return false;
				}
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {

			if (!(damageSource.getAttacker() instanceof PlayerEntity player))
				return;

			if (!player.getMainHandStack().isOf(ModItems.BLOODHARVESTER))
				return;

			BloodharvesterItem.addBloodStacks(player, 5);

		});
	}

	public static void checkTotem() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DawnrenderItem::activateTotem);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
