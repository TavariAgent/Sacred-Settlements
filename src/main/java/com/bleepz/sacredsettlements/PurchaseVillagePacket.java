package com.bleepz.sacredsettlements;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public record PurchaseVillagePacket(BlockPos bellPosition) implements CustomPacketPayload {

    public static final Type<PurchaseVillagePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SacredSettlements.MODID, "purchase_village"));

    public static final StreamCodec<ByteBuf, PurchaseVillagePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PurchaseVillagePacket::bellPosition,
            PurchaseVillagePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PurchaseVillagePacket packet, ServerPlayer player) {
        VillageData village = ProtectionRadiusManager.getInstance().getOrCreateVillageData(packet.bellPosition());

        if (village == null) {
            player.sendSystemMessage(Component.literal("§cVillage not found!"));
            return;
        }

        if (village.hasOwner()) {
            player.sendSystemMessage(Component.literal("§cThis village is already owned!"));
            return;
        }

        int purchasePrice = ProtectionConfig.VILLAGE_PURCHASE_PRICE.get();

        // Check if player has enough emeralds
        int emeraldCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.EMERALD)) {
                emeraldCount += stack.getCount();
            }
        }

        if (emeraldCount < purchasePrice) {
            player.sendSystemMessage(Component.literal("§cYou need " + purchasePrice + " emeralds to purchase this village!"));
            return;
        }

        // Remove emeralds from player inventory
        int toRemove = purchasePrice;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.EMERALD) && toRemove > 0) {
                int removeFromStack = Math.min(toRemove, stack.getCount());
                stack.shrink(removeFromStack);
                toRemove -= removeFromStack;
            }
        }

        // Set ownership
        village.setOwner(player.getUUID(), player.getName().getString());
        player.sendSystemMessage(Component.literal("§aYou now own this village!"));

        // Refresh the GUI
        player.closeContainer();
    }
}
