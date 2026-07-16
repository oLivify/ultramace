package net.oliviy.ultramace.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;
import net.oliviy.ultramace.effects.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Unique
    private static final Identifier SOUL_FULL =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/soul_heart_full.png"
            );

    @Unique
    private static final Identifier SOUL_HALF =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/soul_half_heart.png"
            );

    @Unique
    private static final Identifier SOUL_FULL_BLINK =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/soul_heart_blinking.png"
            );

    @Unique
    private static final Identifier SOUL_HALF_BLINK =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/soul_half_heart_blinking.png"
            );

    @Unique
    private static final Identifier SOUL_EMPTY =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/container.png"
            );
    @Unique
    private static final Identifier SOUL_EMPTY_BLINKING =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/gui/container_blinking.png"
            );




    @Inject(
            method = "drawHeart",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ultramace$customHearts(
            DrawContext context,
            @Coerce Object type,
            int x,
            int y,
            boolean hardcore,
            boolean blinking,
            boolean half,
            CallbackInfo ci
    ) {

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null ||
                !client.player.hasStatusEffect(ModEffects.SOUL_SICKNESS)) {
            return;
        }


        Identifier texture;


        if (type.toString().equals("CONTAINER")) {

            texture = blinking
                    ? SOUL_EMPTY_BLINKING
                    : SOUL_EMPTY;

        }

        // Actual health hearts
        else {

            if (half) {
                texture = blinking
                        ? SOUL_HALF_BLINK
                        : SOUL_HALF;

            } else {
                texture = blinking
                        ? SOUL_FULL_BLINK
                        : SOUL_FULL;
            }
        }


        context.drawTexture(
                texture,
                x,
                y,
                0,
                0,
                9,
                9,
                9,
                9
        );


        ci.cancel();
    }
}