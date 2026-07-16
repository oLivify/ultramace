package net.oliviy.ultramace.item.custom.voidpiercer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CataclysmManager {

    private static final List<CataclysmInstance> ACTIVE_RIFTS = new ArrayList<>();

    /**
     * Call once during mod initialization.
     */
    public static void register() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            Iterator<CataclysmInstance> iterator = ACTIVE_RIFTS.iterator();

            while (iterator.hasNext()) {

                CataclysmInstance instance = iterator.next();

                // Returns true when the rift has finished
                if (instance.tick()) {
                    iterator.remove();
                }
            }
        });
    }

    /**
     * Creates a new Ender's Cataclysm.
     */
    public static void spawn(ServerWorld world, PlayerEntity caster, Vec3d center) {

        ACTIVE_RIFTS.add(
                new CataclysmInstance(
                        world,
                        caster,
                        center
                )
        );
    }
}
