package com.bleepz.sacredsettlements;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class VillageData {
    private final BlockPos bellPosition;
    private UUID ownerId;
    private String ownerName;
    private final ItemStack[] emeraldInventory;
    private boolean isPurchased;
    private long ownershipStartTime;
    private long lastTaxCollectionTime;

    public VillageData(BlockPos bellPosition) {
        this.bellPosition = bellPosition;
        this.emeraldInventory = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            this.emeraldInventory[i] = ItemStack.EMPTY;
        }
        this.isPurchased = false;
        this.ownershipStartTime = 0;
    }

    public BlockPos getBellPosition() {
        return bellPosition;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwner(UUID playerId, String playerName) {
        this.ownerId = playerId;
        this.ownerName = playerName;
        this.isPurchased = true;
        this.ownershipStartTime = System.currentTimeMillis();
        this.lastTaxCollectionTime = System.currentTimeMillis();
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public ItemStack[] getEmeraldInventory() {
        return emeraldInventory;
    }

    public void setInventorySlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 9) {
            emeraldInventory[slot] = stack;
        }
    }

    public int getTotalEmeralds() {
        int total = 0;
        for (ItemStack stack : emeraldInventory) {
            if (stack.is(Items.EMERALD)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public boolean hasOwner() {
        return ownerId != null && isPurchased;
    }

    public long getOwnershipStartTime() {
        return ownershipStartTime;
    }

    public void setOwnershipStartTime(Long time) {
        this.ownershipStartTime = time;
    }

    public long getOwnershipDurationMillis() {
        if (!hasOwner()) {
            return 0;
        }
        return System.currentTimeMillis() - ownershipStartTime;
    }

    public String getOwnershipDurationFormatted() {
        long millis = getOwnershipDurationMillis();
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public long getLastTaxCollectionTime() {
        return lastTaxCollectionTime;
    }

    public void setLastTaxCollectionTime(Long time) {
        this.lastTaxCollectionTime = time;
    }

    public boolean isTaxDue() {
        if (!hasOwner()) return false;

        long hoursSinceLastCollection = (System.currentTimeMillis() - lastTaxCollectionTime) / (1000 * 60 * 60);
        int collectionInterval = ProtectionConfig.TAX_COLLECTION_HOURS.get();

        return hoursSinceLastCollection >= collectionInterval;

    }

    public int calculateTaxOwed() {
        double ratePerHour = ProtectionConfig.TAX_RATE_PER_HOUR.get();
        int collectionInterval = ProtectionConfig.TAX_COLLECTION_HOURS.get();

        return (int) Math.ceil(ratePerHour * collectionInterval);
    }

    public boolean collectTax() {
        if (!isTaxDue()) return true; // No tax due yet

        int taxOwed = calculateTaxOwed();
        int currentEmeralds = getTotalEmeralds();

        if (currentEmeralds < taxOwed) {
            // Insufficient funds - village is abandoned
            return false;
        }

        // Deduct emeralds
        int toRemove = taxOwed;
        for (int i = 0; i < 9 && toRemove > 0; i++) {
            ItemStack stack = emeraldInventory[i];
            if (stack.is(Items.EMERALD)) {
                int removeFromStack = Math.min(toRemove, stack.getCount());
                stack.shrink(removeFromStack);
                toRemove -= removeFromStack;
            }
        }

        // Update last collection time
        lastTaxCollectionTime = System.currentTimeMillis();
        return true;
    }

    public void abandonVillage() {
        this.ownerId = null;
        this.ownerName = null;
        this.isPurchased = false;
        this.ownershipStartTime = 0;
        this.lastTaxCollectionTime = 0;

        // Clear emerald inventory
        for (int i = 0; i < 9; i++) {
            emeraldInventory[i] = ItemStack.EMPTY;
        }
    }

    // Method to count villagers in range
    public int countVillagersInRange(net.minecraft.server.level.ServerLevel level) {
        int radius = ProtectionConfig.PROTECTION_RADIUS.get();
        net.minecraft.world.phys.AABB boundingBox = new net.minecraft.world.phys.AABB(
                bellPosition.offset(-radius, -50, -radius)
        );

        return level.getEntitiesOfClass(
                net.minecraft.world.entity.npc.Villager.class,
                boundingBox
        ).size();
    }
}