package net.oliviy.ultramace.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.oliviy.ultramace.client.ClientCooldownManager;
import net.oliviy.ultramace.cooldown.CooldownManager;
import net.oliviy.ultramace.data.AbilityHudData;
import net.oliviy.ultramace.item.custom.bloodharvester.BloodharvesterItem;
import net.oliviy.ultramace.item.custom.dawnrender.DawnrenderItem;
import net.oliviy.ultramace.item.custom.spectre_staff.SpectreStaffItem;
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
            ),
            new AbilityHudData(
                    "Enders Cataclysm",
                    0x42002D,
                    "voidpiercer_cataclysm",
                    VoidpiercerItem.CATACLYSM_COOLDOWN
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
            ),
            new AbilityHudData(
                    "Celestial Binding",
                    0xFFB000,
                    "dawnrender_celestial_binding",
                    DawnrenderItem.CELESTIAL_BINDING_COOLDOWN
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



    private static final List<AbilityHudData> SPECTRE_ABILITIES = List.of(
            new AbilityHudData(
                    "Sonic Boom",
                    0xc5cdb5,
                    "spectre_sonic_boom",
                    SpectreStaffItem.SONIC_COOLDOWN
            ),
            new AbilityHudData(
                    "Summon Sculk",
                    0x61c3cb,
                    "spectre_summon",
                    SpectreStaffItem.SUMMON_COOLDOWN
            ),
            new AbilityHudData(
                    "Sculk Spread",
                    0x819987,
                    "spectre_spread",
                    SpectreStaffItem.SPREAD_COOLDOWN
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
        } else if (item instanceof SpectreStaffItem) {
            abilities = SPECTRE_ABILITIES;
        }


        else {
            return; // not holding a legendary weapon
        }

        int x = 10;
        int y = 10;

        for (AbilityHudData ability : abilities) {

            boolean ready = !ClientCooldownManager.isOnCooldown(
                    ability.cooldownId
            );

            long remaining = ClientCooldownManager.getRemainingTicks(
                    ability.cooldownId
            );

            String time;
            if (remaining <= 0) {
                time = "Ready";
            } else {
                long seconds = (remaining + 19) / 20;

                long minutes = seconds / 60;
                seconds %= 60;

                if (minutes > 0) {
                    time = minutes + ":" + String.format("%02d", seconds);
                } else {
                    time = seconds + "s";
                }
            }

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
