package net.oliviy.ultramace.cooldown;

import net.minecraft.server.world.ServerWorld;

public class ModCooldowns {

    public static CooldownState get(ServerWorld world) {

        return world.getPersistentStateManager()
                .getOrCreate(
                        CooldownState.TYPE,
                        "weapon_cooldowns"
                );
    }
}
