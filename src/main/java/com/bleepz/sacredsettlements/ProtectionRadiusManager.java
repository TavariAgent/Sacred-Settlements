package com.bleepz.sacredsettlements;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class ProtectionRadiusManager {
    private static ProtectionRadiusManager instance;
    private ServerLevel currentLevel;

    private ProtectionRadiusManager() {
    }

    public static ProtectionRadiusManager getInstance() {
        if (instance == null) {
            instance = new ProtectionRadiusManager();
        }
        return instance;
    }

    public void setLevel(ServerLevel level) {
        this.currentLevel = level;
    }

    private Map<BlockPos, VillageData> getVillages() {
        if (currentLevel != null) {
            return VillageDataStorage.get(currentLevel).getVillages();
        }
        return new HashMap<>();
    }

    public VillageData getOrCreateVillageData(BlockPos bellPos) {
        Map<BlockPos, VillageData> villages = getVillages();
        BlockPos primaryBell = getPrimaryBell(bellPos);

        if (!villages.containsKey(primaryBell)) {
            villages.put(primaryBell.immutable(), new VillageData(primaryBell.immutable()));
        }

        return villages.get(primaryBell);
    }

    public VillageData getVillageData(BlockPos bellPos) {
        return getVillages().get(bellPos);
    }

    public VillageData findNearestVillage(BlockPos pos) {
        int radius = ProtectionConfig.PROTECTION_RADIUS.get();
        int radiusSquared = radius * radius;

        for (VillageData village : getVillages().values()) {
            BlockPos bellPos = village.getBellPosition();
            int dx = pos.getX() - bellPos.getX();
            int dz = pos.getZ() - bellPos.getZ();
            int distanceSquared = dx * dx + dz * dz;

            if (distanceSquared <= radiusSquared) {
                return village;
            }
        }
        return null;
    }

    public BlockPos getPrimaryBell(BlockPos anyBellInVillage) {
        Map<BlockPos, VillageData> villages = getVillages();

        if (villages.containsKey(anyBellInVillage)) {
            return anyBellInVillage;
        }

        int radius = ProtectionConfig.PROTECTION_RADIUS.get();
        int radiusSquared = radius * radius;

        for (BlockPos primaryBell : villages.keySet()) {
            int dx = anyBellInVillage.getX() - primaryBell.getX();
            int dz = anyBellInVillage.getZ() - primaryBell.getZ();
            int distanceSquared = dx * dx + dz * dz;

            if (distanceSquared <= radiusSquared) {
                return primaryBell;
            }
        }

        return anyBellInVillage;
    }

    public boolean isPositionProtected(BlockPos pos) {
        return findNearestVillage(pos) != null;
    }

    public void clearProtectedVillages() {
        getVillages().clear();
    }

    public Map<BlockPos, VillageData> getAllVillages() {
        return new HashMap<>(getVillages());
    }
}
