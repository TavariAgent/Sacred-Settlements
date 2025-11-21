package com.bleepz.sacredsettlements;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import org.jetbrains.annotations.NotNull;

public class VillageBellMenu extends AbstractContainerMenu {
    private VillageData villageData; // Server-side only
    private final BlockPos bellPosition;
    private final SimpleContainer emeraldContainer; // Client and server
    private Player openingPlayer;

    // Client-side synced data
    private String ownerName;
    private long ownershipStartTime;
    private boolean hasOwner;

    // Client-side constructor
    public VillageBellMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
        this.openingPlayer = playerInventory.player;
        // Read synced data from packet
        this.hasOwner = extraData.readBoolean();
        if (this.hasOwner) {
            this.ownerName = extraData.readUtf();
            this.ownershipStartTime = extraData.readLong();
        }
    }

    // Server-side constructor
    @SuppressWarnings("resource")
    public VillageBellMenu(int containerId, Inventory playerInventory, BlockPos bellPos) {
        super(ModMenuTypes.VILLAGE_BELL_MENU.get(), containerId);
        this.bellPosition = bellPos;
        this.emeraldContainer = new SimpleContainer(9);
        this.openingPlayer = playerInventory.player; // Store player

        // Only get village data on server side
        if (!playerInventory.player.level().isClientSide()) {
            this.villageData = ProtectionRadiusManager.getInstance().getOrCreateVillageData(bellPos);

            // Copy emeralds from village data to container for syncing
            for (int i = 0; i < 9; i++) {
                this.emeraldContainer.setItem(i, villageData.getEmeraldInventory()[i]);
            }
        }

        // Add emerald inventory slots (3x3 grid) - now using the synced container
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                this.addSlot(new EmeraldOnlySlot(emeraldContainer, index, 62 + col * 18, 17 + row * 18));
            }
        }

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        // Save emerald container back to village data when GUI closes (server-side only)
        if (!player.level().isClientSide() && villageData != null) {
            for (int i = 0; i < 9; i++) {
                villageData.setInventorySlot(i, emeraldContainer.getItem(i));
            }
        }
    }

    public VillageData getVillageData() {
        return villageData;
    }

    public BlockPos getBellPosition() {
        return bellPosition;
    }

    public SimpleContainer getEmeraldContainer() {
        return emeraldContainer;
    }

    // Client-accessible methods for synced data
    public boolean hasOwner() {
        if (villageData != null) {
            return villageData.hasOwner();
        }
        return hasOwner;
    }

    public String getOwnerName() {
        if (villageData != null) {
            return villageData.getOwnerName();
        }
        return ownerName;
    }

    public String getOwnershipDurationFormatted() {
        long startTime = villageData != null ?
                (System.currentTimeMillis() - villageData.getOwnershipDurationMillis()) :
                ownershipStartTime;

        if (startTime == 0) return "0s";

        long millis = System.currentTimeMillis() - startTime;
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

    public int getTotalEmeralds() {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = emeraldContainer.getItem(i);
            if (stack.is(Items.EMERALD)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(slotStack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.is(Items.EMERALD)) {
                    if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    private class EmeraldOnlySlot extends Slot {
        public EmeraldOnlySlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.is(Items.EMERALD) && canPlayerAccess();
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return canPlayerAccess();
        }

        private boolean canPlayerAccess() {
            if (villageData == null) return true; // Client-side
            if (!villageData.hasOwner()) return true; // Unclaimed - anyone can add

            // Check if opening player is the owner
            return villageData.getOwnerId() != null &&
                    villageData.getOwnerId().equals(openingPlayer.getUUID());
        }
    }
}