package net.oliviy.ultramace.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.oliviy.ultramace.effects.ModEffects;

public class UseItemEvents {

    public static void register() {

        UseItemCallback.EVENT.register(
                (player, world, hand) -> {

                    if (player.hasStatusEffect(ModEffects.PARALYSIS)) {

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
}