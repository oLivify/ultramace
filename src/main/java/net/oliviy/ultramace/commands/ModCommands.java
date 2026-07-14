package net.oliviy.ultramace.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.oliviy.ultramace.cooldown.CooldownState;
import net.oliviy.ultramace.cooldown.ModCooldowns;
import net.oliviy.ultramace.network.payload.CooldownPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("resetcooldowns")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {

                            ServerPlayerEntity player = context.getSource().getPlayer();

                            if (player == null) {
                                return 0;
                            }

                            CooldownState state = ModCooldowns.get(
                                    player.getServerWorld()
                            );


                            // Save the abilities that need to be cleared
                            Map<String, Long> cooldowns =
                                    new HashMap<>(state.getPlayerCooldowns(player.getUuid()));


                            // Clear server cooldowns
                            state.clearPlayerCooldowns(player.getUuid());


                            // Tell client HUD to clear them
                            for (String abilityId : cooldowns.keySet()) {

                                ServerPlayNetworking.send(
                                        player,
                                        new CooldownPayload(
                                                abilityId,
                                                0
                                        )
                                );
                            }


                            context.getSource().sendFeedback(
                                    () -> Text.literal("Cooldowns reset!"),
                                    false
                            );


                            return 1;
                        })
        );
    }


}
