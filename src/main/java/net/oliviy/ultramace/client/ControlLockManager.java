package net.oliviy.ultramace.client;

import net.minecraft.client.MinecraftClient;

public class ControlLockManager {


    private static boolean locked = false;


    public static void lock() {

        locked = true;

    }


    public static void unlock() {

        locked = false;

    }


    public static boolean isLocked() {

        return locked;

    }


    public static void tick(MinecraftClient client) {

        if(!locked) return;

        if(client.player == null) return;


        // Movement
        client.options.forwardKey.setPressed(false);
        client.options.backKey.setPressed(false);
        client.options.leftKey.setPressed(false);
        client.options.rightKey.setPressed(false);


        // Actions
        client.options.jumpKey.setPressed(false);
        client.options.sneakKey.setPressed(false);
        client.options.sprintKey.setPressed(false);



        // Attack/use
        client.options.attackKey.setPressed(false);
        client.options.useKey.setPressed(false);


        // Hotbar scrolling
        client.options.hotbarKeys[0].setPressed(false);
        client.options.hotbarKeys[1].setPressed(false);
        client.options.hotbarKeys[2].setPressed(false);
        client.options.hotbarKeys[3].setPressed(false);
        client.options.hotbarKeys[4].setPressed(false);
        client.options.hotbarKeys[5].setPressed(false);
        client.options.hotbarKeys[6].setPressed(false);
        client.options.hotbarKeys[7].setPressed(false);
        client.options.hotbarKeys[8].setPressed(false);


        // Close container screens
        if(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen){

            client.setScreen(null);

        }


    }


}