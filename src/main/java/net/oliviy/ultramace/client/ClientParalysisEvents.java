package net.oliviy.ultramace.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;


public class ClientParalysisEvents {


    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {


            if(ClientParalysis.paralyzed){

                ClientParalysis.lockCamera();

            }


            ControlLockManager.tick(client);


        });
    }

}