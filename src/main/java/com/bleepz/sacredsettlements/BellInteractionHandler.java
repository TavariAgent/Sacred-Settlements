package com.bleepz.sacredsettlements;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BellInteractionHandler {

    @SubscribeEvent
    public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        // Check if the block is a bell
        if (!state.is(Blocks.BELL)) {
            return;
        }

        // Only handle on server side
        if (level.isClientSide()) {
            event.setCanceled(true);
            return;
        }

        // Make sure the bell is registered and get its village data
        ProtectionRadiusManager manager = ProtectionRadiusManager.getInstance();
        VillageData villageData = manager.getOrCreateVillageData(pos);

        // Open GUI for the player
        if (player instanceof ServerPlayer serverPlayer) {
            openVillageGUI(serverPlayer, villageData);
        }

        // Cancel the event to prevent bell ringing
        event.setCanceled(true);
    }

    private void openVillageGUI(ServerPlayer player, VillageData villageData) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, playerInventory, p) ->
                        new VillageBellMenu(containerId, playerInventory, villageData.getBellPosition()),
                net.minecraft.network.chat.Component.literal("Village Bell")
        ), buf -> {
            buf.writeBlockPos(villageData.getBellPosition());
            buf.writeBoolean(villageData.hasOwner());
            if (villageData.hasOwner()) {
                buf.writeUtf(villageData.getOwnerName());
                // Send the start time, not duration
                buf.writeLong(System.currentTimeMillis() - villageData.getOwnershipDurationMillis());
            }
        });
    }
}