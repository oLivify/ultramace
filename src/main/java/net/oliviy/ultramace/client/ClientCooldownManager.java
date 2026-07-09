package net.oliviy.ultramace.client;

import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class ClientCooldownManager {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    public static void setCooldown(String abilityId, long endTick) {
        COOLDOWNS.put(abilityId, endTick);
    }

    public static long getRemainingTicks(String abilityId) {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null)
            return 0;

        Long endTick = COOLDOWNS.get(abilityId);

        if (endTick == null)
            return 0;

        return Math.max(0, endTick - client.world.getTime());
    }

    public static boolean isOnCooldown(String abilityId) {
        return getRemainingTicks(abilityId) > 0;
    }
}
