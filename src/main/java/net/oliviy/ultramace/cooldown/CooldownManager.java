package net.oliviy.ultramace.cooldown;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Player UUID -> (Ability ID -> Last Use Tick)
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    /**
     * Starts a cooldown for an ability.
     *
     * @param player The player.
     * @param abilityId A unique ID, e.g. "dawnrender_judgment".
     */
    public static void startCooldown(PlayerEntity player, String abilityId) {

        COOLDOWNS
                .computeIfAbsent(player.getUuid(), uuid -> new HashMap<>())
                .put(abilityId, player.getWorld().getTime());
    }

    /**
     * Returns true if the ability is currently on cooldown.
     */
    public static boolean isOnCooldown(PlayerEntity player, String abilityId, long cooldownTicks) {

        Map<String, Long> playerCooldowns = COOLDOWNS.get(player.getUuid());

        if (playerCooldowns == null) {
            return false;
        }

        Long lastUse = playerCooldowns.get(abilityId);

        if (lastUse == null) {
            return false;
        }

        long currentTick = player.getWorld().getTime();

        return currentTick - lastUse < cooldownTicks;
    }

    /**
     * Returns remaining cooldown in ticks.
     */
    public static long getRemainingTicks(PlayerEntity player, String abilityId, long cooldownTicks) {

        Map<String, Long> playerCooldowns = COOLDOWNS.get(player.getUuid());

        if (playerCooldowns == null) {
            return 0;
        }

        Long lastUse = playerCooldowns.get(abilityId);

        if (lastUse == null) {
            return 0;
        }

        long elapsed = player.getWorld().getTime() - lastUse;

        return Math.max(0, cooldownTicks - elapsed);
    }

    /**
     * Removes a cooldown.
     */
    public static void clearCooldown(PlayerEntity player, String abilityId) {

        Map<String, Long> playerCooldowns = COOLDOWNS.get(player.getUuid());

        if (playerCooldowns != null) {
            playerCooldowns.remove(abilityId);

            if (playerCooldowns.isEmpty()) {
                COOLDOWNS.remove(player.getUuid());
            }
        }
    }

    /**
     * Returns true if the ability is ready to use.
     */
    public static boolean isReady(PlayerEntity player, String abilityId, long cooldownTicks) {
        return !isOnCooldown(player, abilityId, cooldownTicks);
    }

    /**
     * Remaining cooldown in seconds.
     */
    public static int getRemainingSeconds(PlayerEntity player, String abilityId, long cooldownTicks) {
        return (int) Math.ceil(getRemainingTicks(player, abilityId, cooldownTicks) / 20.0);
    }

    public static String getFormattedTime(PlayerEntity player,
                                          String abilityId,
                                          long cooldownTicks) {

        long remaining = getRemainingTicks(player, abilityId, cooldownTicks);

        if (remaining <= 0) {
            return "Ready";
        }

        long seconds = (remaining + 19) / 20;

        long minutes = seconds / 60;
        seconds %= 60;

        if (minutes > 0) {
            return minutes + ":" + String.format("%02d", seconds);
        }

        return seconds + "s";
    }
}
