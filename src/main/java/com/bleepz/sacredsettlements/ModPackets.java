package com.bleepz.sacredsettlements;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SacredSettlements.MODID)
public class ModPackets {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SacredSettlements.MODID);

        registrar.playToServer(
                PurchaseVillagePacket.TYPE,
                PurchaseVillagePacket.CODEC,
                (packet, context) -> context.enqueueWork(() ->
                        PurchaseVillagePacket.handle(packet, (ServerPlayer) context.player())
                )
        );
    }
}