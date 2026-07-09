package net.oliviy.ultramace.data;

import java.util.UUID;
import java.util.function.Function;

public class AbilityHudData {

    public final String name;
    public final int color;
    public final String cooldownId;
    public final long cooldownTicks;

    public AbilityHudData(String name, int color, String cooldownId, long cooldownTicks) {
        this.name = name;
        this.color = color;
        this.cooldownId = cooldownId;
        this.cooldownTicks = cooldownTicks;
    }


}