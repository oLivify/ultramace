package net.oliviy.ultramace.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.oliviy.ultramace.effects.ModEffects;

public class AttackEvents {


    public static void register() {

        AttackEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {


                    if(player.hasStatusEffect(ModEffects.PARALYSIS)) {

                        return ActionResult.FAIL;

                    }


                    return ActionResult.PASS;

                }
        );

    }

}