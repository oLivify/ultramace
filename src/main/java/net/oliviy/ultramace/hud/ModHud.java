package net.oliviy.ultramace.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.oliviy.ultramace.cooldown.CooldownManager;
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
                    "voidpiercer_blink",
                    VoidpiercerItem.BLINK_COOLDOWN
            ),
            new AbilityHudData(
                    "Rift Slash",
                    0xFF00AA,
                    "voidpiercer_rift",
                    VoidpiercerItem.RIFT_COOLDOWN
            )
    );

    private static final List<AbilityHudData> DAWNRENDER_ABILITIES = List.of(
            new AbilityHudData(
                    "Divine Cleave",
                    0xFFD700,
                    "dawnrender_divine_cleave",
                    DawnrenderItem.DIVINE_CLEAVE_COOLDOWN
            ),
            new AbilityHudData(
                    "Hero's Resolve",
                    0xFFF4A3,
                    "dawnrender_dawn_totem",
                    DawnrenderItem.DAWN_TOTEM_COOLDOWN
            )
    );

    private static final List<AbilityHudData> STORMCLEAVER_ABILITIES = List.of(
            new AbilityHudData(
                    "Thunder Strike",
                    0x7DF9FF,
                    "stormcleaver_thunder",
                    StormcleaverItem.THUNDER_COOLDOWN
            )
    );

    private static final List<AbilityHudData> STARFALL_ABILITIES = List.of(
            new AbilityHudData(
                    "Meteor Shower",
                    0xFFAA00,
                    "starfall_meteor",
                    StarfallItem.METEOR_COOLDOWN
            ),
            new AbilityHudData(
                    "Starquake",
                    0x55FFFF,
                    "starfall_starquake",
                    StarfallItem.STARQUAKE_COOLDOWN
            ),
            new AbilityHudData(
                    "Heaven's Judgment",
                    0xFF55FF,
                    "starfall_ultimate", StarfallItem.ULTIMATE_COOLDOWN
            )
    );

    private static final List<AbilityHudData> BLOODHARVESTER_ABILITIES = List.of(

            new AbilityHudData(
                    "Blood Rite",
                    0xB22222,
                    "bloodharvester_blood_rite",
                    BloodharvesterItem.BLOOD_RITE_COOLDOWN
            ),

            new AbilityHudData(
                    "Executioner's Sweep",
                    0xFF1A1A,
                    "bloodharvester_sweep",
                    BloodharvesterItem.SWEEP_COOLDOWN
            ),

            new AbilityHudData(
                    "Reaper's Domain",
                    0xDA70D6,
                    "bloodharvester_domain",
                    BloodharvesterItem.DOMAIN_COOLDOWN
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

        int x = 10;
        int y = 10;

        for (AbilityHudData ability : abilities) {

            boolean ready = CooldownManager.isReady(
                    player,
                    ability.cooldownId,
                    ability.cooldownTicks
            );

            String time = CooldownManager.getFormattedTime(
                    player,
                    ability.cooldownId,
                    ability.cooldownTicks
            );

            context.drawText(
                    client.textRenderer,
                    ability.name + ": " + time,
                    x,
                    y,
                    ready ? 0x55FF55 : ability.color,
                    true
            );

            y += 10;
        }


    }
}
