package net.oliviy.ultramace.item.custom.dawnrender;


import net.minecraft.entity.Entity;

import java.util.*;

public class FreezeManager {

    private static final Map<UUID, Long> FROZEN_PLAYERS = new HashMap<>();


    public static void freeze(Entity entity, long durationTicks) {

        FROZEN_PLAYERS.put(
                entity.getUuid(),
                entity.getWorld().getTime() + durationTicks
        );
    }


    public static boolean isFrozen(Entity entity) {

        Long endTime = FROZEN_PLAYERS.get(entity.getUuid());

        if(endTime == null) {
            return false;
        }


        if(entity.getWorld().getTime() >= endTime) {

            FROZEN_PLAYERS.remove(entity.getUuid());
            return false;
        }


        return true;
    }


    public static void unfreeze(Entity entity) {

        FROZEN_PLAYERS.remove(entity.getUuid());

    }
}
