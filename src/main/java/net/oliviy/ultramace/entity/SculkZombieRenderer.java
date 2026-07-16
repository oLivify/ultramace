package net.oliviy.ultramace.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public class SculkZombieRenderer extends ZombieEntityRenderer {
    private static final Identifier TEXTURE =
            Identifier.of(
                    Ultramace.MOD_ID,
                    "textures/entity/zombie/sculk_zombie.png"
            );


    public SculkZombieRenderer(EntityRendererFactory.Context context) {
        super(context);
    }


    @Override
    public Identifier getTexture(ZombieEntity entity) {
        return TEXTURE;
    }
}
