package net.oliviy.ultramace.data;

import java.util.UUID;
import java.util.function.Function;

public class AbilityHudData {

    public final String name;
    public final int color;
    public final long cooldownMs;
    public final Function<UUID, Long> lastUseGetter;

    public AbilityHudData(String name, int color, long cooldownMs,
                          Function<UUID, Long> lastUseGetter) {
        this.name = name;
        this.color = color;
        this.cooldownMs = cooldownMs;
        this.lastUseGetter = lastUseGetter;
    }
}