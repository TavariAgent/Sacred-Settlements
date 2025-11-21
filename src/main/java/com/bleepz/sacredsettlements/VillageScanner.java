package com.bleepz.sacredsettlements;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

public class VillageScanner {

    public static void scanChunkForVillage(LevelChunk chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();

        // Scan all block positions in this chunk for bells
        // Villages typically generate between Y 60-120
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y < 320; y++) { // Full world height range for 1.21
                    BlockPos pos = chunkPos.getBlockAt(x, y, z);

                    if (serverLevel.getBlockState(pos).is(Blocks.BELL)) {
                        ProtectionRadiusManager.getInstance().getOrCreateVillageData(pos);
                        // Found a bell in this chunk, no need to keep scanning
                        return;
                    }
                }
            }
        }
    }
}