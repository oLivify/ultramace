package net.oliviy.ultramace.cooldown;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.oliviy.ultramace.network.payload.CooldownPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownState extends PersistentState {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static final Type<CooldownState> TYPE =
            new Type<>(
                    CooldownState::new,
                    CooldownState::fromNbt,
                    null
            );


    public void setCooldown(UUID player, String ability, long endTick) {

        cooldowns
                .computeIfAbsent(player, k -> new HashMap<>())
                .put(ability, endTick);

        markDirty();


    }


    public boolean isOnCooldown(UUID player, String ability, long currentTick) {

        Map<String, Long> playerCooldowns = cooldowns.get(player);

        if (playerCooldowns == null) {
            return false;
        }

        Long end = playerCooldowns.get(ability);

        if (end == null) {
            return false;
        }

        return currentTick < end;
    }


    public long getRemaining(UUID player, String ability, long currentTick) {

        Map<String, Long> playerCooldowns = cooldowns.get(player);

        if (playerCooldowns == null) {
            return 0;
        }

        Long end = playerCooldowns.get(ability);

        if (end == null) {
            return 0;
        }

        return Math.max(0, end - currentTick);
    }


    public static CooldownState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        CooldownState state = new CooldownState();

        NbtList players = nbt.getList("Players", NbtElement.COMPOUND_TYPE);

        for (NbtElement element : players) {

            NbtCompound playerTag = (NbtCompound) element;

            UUID uuid = playerTag.getUuid("UUID");

            NbtCompound abilities = playerTag.getCompound("Abilities");

            Map<String, Long> map = new HashMap<>();

            for (String key : abilities.getKeys()) {
                map.put(
                        key,
                        abilities.getLong(key)
                );
            }

            state.cooldowns.put(uuid, map);


        }



        return state;
    }

    public Map<String, Long> getPlayerCooldowns(UUID uuid) {
        return cooldowns.getOrDefault(uuid, new HashMap<>());
    }


    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        NbtList players = new NbtList();

        for (var playerEntry : cooldowns.entrySet()) {

            NbtCompound playerTag = new NbtCompound();

            playerTag.putUuid(
                    "UUID",
                    playerEntry.getKey()
            );

            NbtCompound abilities = new NbtCompound();

            for (var ability : playerEntry.getValue().entrySet()) {

                abilities.putLong(
                        ability.getKey(),
                        ability.getValue()
                );
            }
            playerTag.put(
                    "Abilities",
                    abilities
            );

            players.add(playerTag);

        }

        nbt.put(
                "Players",
                players
        );



        return nbt;
    }

    public void cleanup(long currentTick) {

        cooldowns.values().forEach(map ->
                map.entrySet().removeIf(entry ->
                        entry.getValue() <= currentTick
                )
        );

        cooldowns.entrySet().removeIf(
                entry -> entry.getValue().isEmpty()
        );

        markDirty();
    }

    public void clearPlayerCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
        markDirty();
    }


    public void syncPlayer(ServerPlayerEntity player) {

        Map<String, Long> playerCooldowns =
                cooldowns.get(player.getUuid());



        if (playerCooldowns == null) {
            return;
        }

        for (var entry : playerCooldowns.entrySet()) {



            ServerPlayNetworking.send(
                    player,
                    new CooldownPayload(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }
    }

}
