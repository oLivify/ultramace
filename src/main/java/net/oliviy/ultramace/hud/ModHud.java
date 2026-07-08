package net.oliviy.ultramace.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.oliviy.ultramace.data.AbilityHudData;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.starfall.StarfallItem;
import net.oliviy.ultramace.item.custom.stormcleaver.StormcleaverItem;
import net.oliviy.ultramace.item.custom.voidpiercer.VoidpiercerItem;

import java.util.List;
import java.util.UUID;

public class ModHud implements HudRenderCallback {

    private static final List<AbilityHudData> VOIDPIERCER_ABILITIES = List.of(
            new AbilityHudData(
                    "Blink Cut",
                    0xAA00FF,
                    6000,
                    id -> VoidpiercerItem.BLINK_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),
            new AbilityHudData(
                    "Rift Slash",
                    0xFF00AA,
                    10000,
                    id -> VoidpiercerItem.RIFT_COOLDOWN_MAP.getOrDefault(id, 0L)
            )
    );

    private static final List<AbilityHudData> DAWNRENDER_ABILITIES = List.of(
            new AbilityHudData(
                    "Divine Cleave",
                    0xFFD700,
                    120000L,
                    id -> DawnrenderItem.DIVINE_CLEAVE_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),
            new AbilityHudData(
                    "Dawn Totem",
                    0xFFD700,
                    90000L,
                    id -> DawnrenderItem.DAWN_TOTEM_COOLDOWN_MAP.getOrDefault(id, 0L)
            )
    );

    private static final List<AbilityHudData> STORMCLEAVER_ABILITIES = List.of(
            new AbilityHudData(
                    "Thunder Strike",
                    0x7DF9FF,
                    15000,
                    id -> StormcleaverItem.THUNDER_STRIKE_COOLDOWN_MAP.getOrDefault(id, 0L)
            )
    );

    private static final List<AbilityHudData> STARFALL_ABILITIES = List.of(
            new AbilityHudData(
                    "Meteor Shower",
                    0xFFAA00,
                    20000,
                    id -> StarfallItem.METEOR_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),
            new AbilityHudData(
                    "Starquake",
                    0x55FFFF,
                    30000,
                    id -> StarfallItem.STARQUAKE_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),
            new AbilityHudData(
                    "Heaven's Judgment",
                    0xFF55FF,
                    60000,
                    id -> StarfallItem.ULTIMATE_COOLDOWN_MAP.getOrDefault(id, 0L)
            )
    );

    private static final List<AbilityHudData> BLOODHARVESTER_ABILITIES = List.of(

            new AbilityHudData(
                    "Blood Rite",
                    0x8B0000, // deep blood red
                    10000,
                    id -> BloodharvesterItem.BLOOD_RITE_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),

            new AbilityHudData(
                    "Executioner's Sweep",
                    0xFF1A1A, // sharp crimson red
                    6000,
                    id -> BloodharvesterItem.SWEEP_COOLDOWN_MAP.getOrDefault(id, 0L)
            ),

            new AbilityHudData(
                    "Reaper's Domain",
                    0x550000, // dark void-red
                    20000,
                    id -> BloodharvesterItem.DOMAIN_COOLDOWN_MAP.getOrDefault(id, 0L)
            )
    );

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null) return;


        List<AbilityHudData> abilities = null;

        Item item = player.getMainHandStack().getItem();

        if (item instanceof VoidpiercerItem) {
            abilities = VOIDPIERCER_ABILITIES;
        }
        else if (item instanceof DawnrenderItem) {
            abilities = DAWNRENDER_ABILITIES;
        } else if (item instanceof StormcleaverItem) {
            abilities = STORMCLEAVER_ABILITIES;
        } else if (item instanceof StarfallItem) {
            abilities = STARFALL_ABILITIES;
        } else if (item instanceof BloodharvesterItem) {
            abilities = BLOODHARVESTER_ABILITIES;
        }


        else {
            return; // not holding a legendary weapon
        }

        UUID id = player.getUuid();
        long now = System.currentTimeMillis();

        int x = 10;
        int y = 10;

        for (AbilityHudData ability : abilities) {

            long last = ability.lastUseGetter.apply(id);
            long left = (last + ability.cooldownMs) - now;

            if (left > 0) {
                context.drawText(
                        client.textRenderer,
                        ability.name + ": " + String.format("%.1fs", left / 1000.0f),
                        x,
                        y,
                        ability.color,
                        true
                );
            } else {
                context.drawText(
                        client.textRenderer,
                        ability.name + ": READY",
                        x,
                        y,
                        0x55FF55,
                        true
                );
            }

            y += 10;
        }


    }
}
