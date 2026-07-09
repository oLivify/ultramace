package net.oliviy.ultramace.client;

import net.minecraft.client.MinecraftClient;

public class ClientParalysis {

    public static boolean paralyzed = false;

    private static float lockedYaw;
    private static float lockedPitch;


    public static void setParalyzed(boolean value) {

        MinecraftClient client = MinecraftClient.getInstance();


        if(value && !paralyzed){

            if(client.player != null){

                lockedYaw = client.player.getYaw();
                lockedPitch = client.player.getPitch();

            }
            ControlLockManager.lock();

        }

        if(!value){

            ControlLockManager.unlock();

        }
        paralyzed = value;

    }


    public static void lockCamera() {

        MinecraftClient client = MinecraftClient.getInstance();

        if(client.player == null) return;


        client.player.setYaw(lockedYaw);
        client.player.setPitch(lockedPitch);

    }

    public static void disableControls() {

        MinecraftClient client = MinecraftClient.getInstance();

        if(client.player == null) return;


        client.options.forwardKey.setPressed(false);
        client.options.backKey.setPressed(false);
        client.options.leftKey.setPressed(false);
        client.options.rightKey.setPressed(false);

        client.options.jumpKey.setPressed(false);
        client.options.sneakKey.setPressed(false);

        client.options.sprintKey.setPressed(false);

        client.options.swapHandsKey.setPressed(false);

        client.options.dropKey.setPressed(false);


    }

}
