package com.bleepz.sacredsettlements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class VillageDataStorage {
    private static final String DATA_NAME = "sacredsettlements_villages";

    private final Map<BlockPos, VillageData> villages = new HashMap<>();

    public VillageDataStorage() {
    }

    public static VillageDataStorage load(CompoundTag tag, HolderLookup.Provider provider) {
        VillageDataStorage storage = new VillageDataStorage();

        if (!tag.contains("Villages")) {
            return storage;
        }

        ListTag villageList = tag.getList("Villages", 10); // 10 = TAG_COMPOUND

        for (int i = 0; i < villageList.size(); i++) {
            CompoundTag villageTag = villageList.getCompound(i);

            // Read bell position - no orElse needed
            BlockPos bellPos = new BlockPos(
                    villageTag.getInt("BellX"),
                    villageTag.getInt("BellY"),
                    villageTag.getInt("BellZ")
            );

            VillageData village = new VillageData(bellPos);

            // Read ownership data
            if (villageTag.getBoolean("HasOwner")) {
                // Read UUID from two longs
                long mostSigBits = villageTag.getLong("OwnerIdMost");
                long leastSigBits = villageTag.getLong("OwnerIdLeast");
                UUID ownerId = new UUID(mostSigBits, leastSigBits);

                String ownerName = villageTag.getString("OwnerName");
                long ownershipStart = villageTag.getLong("OwnershipStart");
                long lastTaxCollection = villageTag.getLong("LastTaxCollection");

                village.setOwner(ownerId, ownerName);
                village.setOwnershipStartTime(ownershipStart);
                village.setLastTaxCollectionTime(lastTaxCollection);
            }

            // Read emerald inventory
            ListTag inventoryList = villageTag.getList("Inventory", 10);
            for (int j = 0; j < inventoryList.size(); j++) {
                CompoundTag itemTag = inventoryList.getCompound(j);
                int slot = itemTag.getInt("Slot");
                CompoundTag itemData = itemTag.getCompound("Item");

                if (!itemData.isEmpty()) {
                    var ops = provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                    ItemStack.CODEC.parse(ops, itemData)
                            .resultOrPartial(error -> {
                            })
                            .ifPresent(stack -> village.setInventorySlot(slot, stack));
                }
            }

            storage.villages.put(bellPos, village);
        }

        return storage;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag villageList = new ListTag();

        for (VillageData village : villages.values()) {
            CompoundTag villageTag = new CompoundTag();

            // Save bell position
            BlockPos bellPos = village.getBellPosition();
            villageTag.putInt("BellX", bellPos.getX());
            villageTag.putInt("BellY", bellPos.getY());
            villageTag.putInt("BellZ", bellPos.getZ());

            // Save ownership data
            villageTag.putBoolean("HasOwner", village.hasOwner());
            if (village.hasOwner()) {
                // Save UUID as two longs
                UUID ownerId = village.getOwnerId();
                villageTag.putLong("OwnerIdMost", ownerId.getMostSignificantBits());
                villageTag.putLong("OwnerIdLeast", ownerId.getLeastSignificantBits());

                villageTag.putString("OwnerName", village.getOwnerName());
                villageTag.putLong("OwnershipStart", village.getOwnershipStartTime());
                villageTag.putLong("LastTaxCollection", village.getLastTaxCollectionTime());
            }

            // Save emerald inventory
            ListTag inventoryList = new ListTag();
            ItemStack[] inventory = village.getEmeraldInventory();
            for (int i = 0; i < inventory.length; i++) {
                if (!inventory[i].isEmpty()) {
                    CompoundTag itemTag = new CompoundTag(); // YOU NEED THIS LINE!
                    itemTag.putInt("Slot", i);

                    var ops = provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                    CompoundTag itemNbt = (CompoundTag) ItemStack.CODEC.encodeStart(ops, inventory[i])
                            .getOrThrow();
                    itemTag.put("Item", itemNbt);
                    inventoryList.add(itemTag); // AND THIS LINE!
                }
            }
            villageTag.put("Inventory", inventoryList);

            villageList.add(villageTag);
        }

        tag.put("Villages", villageList);
        return tag;
    }

    public Map<BlockPos, VillageData> getVillages() {
        return villages;
    }

    private static VillageDataStorage instance;

    public static VillageDataStorage get(ServerLevel level) {
        if (instance == null) {
            instance = new VillageDataStorage();
            instance.loadFromFile(level);
        }
        return instance;
    }

    public void loadFromFile(ServerLevel level) {
        try {
            java.nio.file.Path dataDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data");
            java.nio.file.Path dataFile = dataDir.resolve(DATA_NAME + ".dat");

            if (java.nio.file.Files.exists(dataFile)) {
                CompoundTag tag = net.minecraft.nbt.NbtIo.readCompressed(dataFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                VillageDataStorage loaded = load(tag, level.registryAccess());
                this.villages.putAll(loaded.villages);
                SacredSettlements.LOGGER.info("Loaded {} villages from storage", villages.size());
            }
        } catch (Exception e) {
            SacredSettlements.LOGGER.error("Failed to load village data", e);
        }
    }

    public void saveToFile(ServerLevel level) {
        try {
            java.nio.file.Path dataDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data");
            java.nio.file.Files.createDirectories(dataDir);

            java.nio.file.Path dataFile = dataDir.resolve(DATA_NAME + ".dat");
            CompoundTag tag = save(new CompoundTag(), level.registryAccess());
            net.minecraft.nbt.NbtIo.writeCompressed(tag, dataFile);
            SacredSettlements.LOGGER.info("Saved {} villages to storage", villages.size());
        } catch (Exception e) {
            SacredSettlements.LOGGER.error("Failed to save village data", e);
        }
    }

    public static void reset() {
        instance = null;
    }
}