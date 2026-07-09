package net.oliviy.ultramace.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public record CooldownPayload(String abilityId, long endTick)
        implements CustomPayload {

    public static final Id<CooldownPayload> ID =
            new Id<>(Identifier.of(Ultramace.MOD_ID, "cooldown"));

    public static final PacketCodec<RegistryByteBuf, CooldownPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    CooldownPayload::abilityId,

                    PacketCodecs.VAR_LONG,
                    CooldownPayload::endTick,

                    CooldownPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
