package com.bleepz.sacredsettlements;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ProtectionEventHandler {

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Scan chunk for villages when it loads
        VillageScanner.scanChunkForVillage((LevelChunk) event.getChunk());
    }

    @SubscribeEvent
    public void onBellBreak(BlockEvent.BreakEvent event) {
        // Check if the block being broken is a bell
        if (event.getState().is(net.minecraft.world.level.block.Blocks.BELL)) {
            Player player = event.getPlayer();

            // Only allow level 4 admins (higher than regular ops) to break bells
            if (player instanceof ServerPlayer serverPlayer) {
                if (!serverPlayer.hasPermissions(4)) {
                    event.setCanceled(true);
                    player.displayClientMessage(
                            Component.literal("§cVillage bells cannot be destroyed!"),
                            true
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        // Allow admins to break blocks
        if (isAdmin(player)) {
            return;
        }

        // Check if block is in protected chunk
        if (ProtectionRadiusManager.getInstance().isPositionProtected(event.getPos())) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal("§cThis village is protected! You cannot break blocks here."),
                    true
            );
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Allow admins to place blocks
        if (isAdmin(player)) {
            return;
        }

        // Check if block is in protected chunk
        if (ProtectionRadiusManager.getInstance().isPositionProtected(event.getPos())) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal("§cThis village is protected! You cannot place blocks here."),
                    true
            );
        }
    }

    @SubscribeEvent
    public void onEntityDamage(LivingIncomingDamageEvent event) {
        // Check if entity is a villager or iron golem
        if (!(event.getEntity() instanceof Villager) && !(event.getEntity() instanceof IronGolem)) {
            return;
        }

        // Check if entity is in protected area (around a bell)
        if (ProtectionRadiusManager.getInstance().isPositionProtected(event.getEntity().blockPosition())) {
            event.setCanceled(true);
        }
    }

    private boolean isAdmin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.hasPermissions(2); // Permission level 2 = op
        }
        return false;
    }
}