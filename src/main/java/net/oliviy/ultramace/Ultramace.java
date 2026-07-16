package net.oliviy.ultramace;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import net.minecraft.util.TypedActionResult;
import net.oliviy.ultramace.commands.ModCommands;
import net.oliviy.ultramace.cooldown.CooldownState;
import net.oliviy.ultramace.cooldown.ModCooldowns;
import net.oliviy.ultramace.effects.ModEffects;
import net.oliviy.ultramace.entity.ModEntities;
import net.oliviy.ultramace.event.AttackEvents;
import net.oliviy.ultramace.event.ParalysisEvents;
import net.oliviy.ultramace.event.UseItemEvents;
import net.oliviy.ultramace.hud.ModHud;
import net.oliviy.ultramace.item.ItemAttributeManager;
import net.oliviy.ultramace.item.ModItems;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.dawnrender.FreezeManager;
import net.oliviy.ultramace.item.custom.spectre_staff.SpectreStaffItem;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;
import net.oliviy.ultramace.item.custom.stormcleaver.StormcleaverItem;
import net.oliviy.ultramace.item.custom.voidpiercer.CataclysmManager;
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
		ParalysisEvents.register();
		AttackEvents.register();
		UseItemEvents.register();
		CataclysmManager.register();
		ModEntities.registerModEntities();
		register();
		checkTotem();
		clientEvents();
		HudRenderCallback.EVENT.register(new ModHud());
		StarfallItem.initScheduler();

	}

	public static void register() {

		AttackEntityCallback.EVENT.register(
				(player, world, hand, entity, hitResult) -> {

					if (!world.isClient()
							&& player.getStackInHand(hand).getItem() instanceof SpectreStaffItem
							&& entity instanceof LivingEntity target) {


						System.out.println("COMMANDING MINIONS");


						SpectreStaffItem.commandMinions(player, target);
					}


					return ActionResult.PASS;
				}
		);


		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

			if (entity instanceof PlayerEntity) {

				if (source.isOf(DamageTypes.LIGHTNING_BOLT) && StormcleaverItem.LIGHTNING_IMMUNE.contains(entity.getUuid())) {
					return false; // CANCEL DAMAGE
				}
			}

			return true;
		});

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> {
					ModCommands.register(dispatcher);
				}
		);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

			if (entity instanceof PlayerEntity) {

			if (source.isOf(DamageTypes.INDIRECT_MAGIC) && StarfallItem.BREATH_IMMUNE.contains(entity.getUuid())) {
					return false;
				}
			}

			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> { // adds two blood stacks when the player kills an entity

			if (!(damageSource.getAttacker() instanceof PlayerEntity player))
				return;

			if (!player.getMainHandStack().isOf(ModItems.BLOODHARVESTER))
				return;

			BloodharvesterItem.addBloodStacks(player, 2);

		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {

			for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

				ItemAttributeManager.tick(player);

				if (server.getTicks() % 6000 == 0) {
					for (ServerWorld world : server.getWorlds()) {
						ModCooldowns.get(world).cleanup(world.getTime());
					}
				}

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

		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> {
					ServerPlayerEntity player = handler.player;

					CooldownState state =
							ModCooldowns.get(player.getServerWorld());

					state.syncPlayer(player);
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


	}

	public static void checkTotem() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DawnrenderItem::activateTotem);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
