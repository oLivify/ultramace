package net.oliviy.ultramace;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import net.minecraft.util.TypedActionResult;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.hud.ModHud;
import net.oliviy.ultramace.item.ItemAttributeManager;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.dawnrender.FreezeManager;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;
import net.oliviy.ultramace.item.custom.stormcleaver.StormcleaverItem;
import net.oliviy.ultramace.network.ModNetworking;
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
		ModNetworking.register();
		register();
		checkTotem();
		clientEvents();
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

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> { // adds two blood stacks when the player kills a entity

			if (!(damageSource.getAttacker() instanceof PlayerEntity player))
				return;

			if (!player.getMainHandStack().isOf(ModItems.BLOODHARVESTER))
				return;

			BloodharvesterItem.addBloodStacks(player, 2);

		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {

			for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

				ItemAttributeManager.tick(player);

			}

		});

		AttackEntityCallback.EVENT.register(
				(player, world, hand, entity, hitResult) -> {


					if(player.hasStatusEffect(ModEffects.PARALYSIS)) {

						return ActionResult.FAIL;

					}


					return ActionResult.PASS;
				}
		);

		UseItemCallback.EVENT.register(
				(player, world, hand) -> {


					if(player.hasStatusEffect(ModEffects.PARALYSIS)) {

						return TypedActionResult.fail(
								player.getStackInHand(hand)
						);

					}


					return TypedActionResult.pass(
							player.getStackInHand(hand)
					);
				}
		);
	}


	public static void clientEvents() {

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if(client.player == null)
				return;


			if(FreezeManager.isFrozen(client.player)) {

				client.player.input.movementForward = 0;
				client.player.input.movementSideways = 0;

				client.player.setVelocity(0,0,0);
			}

		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if(client.player == null)
				return;


			if(FreezeManager.isFrozen(client.player)) {

				if(client.currentScreen != null) {

					client.setScreen(null);

				}
			}

		});

		AttackBlockCallback.EVENT.register(
				(player, world, hand, pos, direction) -> {

					if(FreezeManager.isFrozen(player)) {
						return ActionResult.FAIL;
					}

					return ActionResult.PASS;
				}
		);

		AttackEntityCallback.EVENT.register(
				(player, world, hand, entity, hitResult) -> {

					if(FreezeManager.isFrozen(player)) {
						return ActionResult.FAIL;
					}

					return ActionResult.PASS;
				}
		);


	}

	public static void checkTotem() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DawnrenderItem::activateTotem);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
