package net.oliviy.ultramace.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.oliviy.ultramace.Ultramace;

public record ParalysisPayload(boolean paralyzed)
        implements CustomPayload {


    public static final Id<ParalysisPayload> ID =
            new Id<>(
                    Identifier.of(Ultramace.MOD_ID, "paralysis")
            );


    public static final PacketCodec<RegistryByteBuf, ParalysisPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL,
                    ParalysisPayload::paralyzed,
                    ParalysisPayload::new
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
