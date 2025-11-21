package com.bleepz.sacredsettlements;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

public class UnstuckCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sacred")
                        .then(Commands.literal("unstuck")
                                .executes(UnstuckCommand::executeUnstuck)
                        )
        );
    }

    private static int executeUnstuck(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command"));
            return 0;
        }

        // Find nearest village
        VillageData village = ProtectionRadiusManager.getInstance()
                .findNearestVillage(player.blockPosition());

        if (village == null) {
            player.sendSystemMessage(Component.literal("§cYou must be in a protected village to use this command!"));
            return 0;
        }

        // Find safe teleport position near bell
        BlockPos bellPos = village.getBellPosition();
        BlockPos safePos = findSafePosition((ServerLevel) player.level(), bellPos);

        if (safePos == null) {
            player.sendSystemMessage(Component.literal("§cCouldn't find a safe location near the bell!"));
            return 0;
        }

        // Teleport player
        player.teleportTo(
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5
        );

        player.sendSystemMessage(Component.literal("§aReturned to village bell!"));
        return 1;
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos bellPos) {
        // Check positions around and above the bell
        for (int yOffset = 0; yOffset <= 5; yOffset++) {
            for (int xOffset = -2; xOffset <= 2; xOffset++) {
                for (int zOffset = -2; zOffset <= 2; zOffset++) {
                    BlockPos checkPos = bellPos.offset(xOffset, yOffset, zOffset);

                    // Check if position is safe (solid block below, 2 air blocks above)
                    if (isSafePosition(level, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        // Fallback to bell position
        return bellPos.above();
    }

    private static boolean isSafePosition(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockPos above = pos.above();

        // Need solid ground below
        if (!level.getBlockState(below).canOcclude()) {
            return false;
        }

        // Need air at position and above
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(above).isAir()) {
            return false;
        }

        // Check for dangerous blocks
        return !level.getBlockState(below).is(Blocks.LAVA) &&
                !level.getBlockState(below).is(Blocks.FIRE);
    }
}
