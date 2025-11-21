package com.bleepz.sacredsettlements;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TaxCollectionSystem {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 1200; // Check every 60 seconds (20 ticks/sec * 60)
    private static int saveCounter = 0;
    private static final int SAVE_INTERVAL = 6000; // Save every 5 minutes (6000 ticks)

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        saveCounter++;

        // Auto-save every 5 minutes
        if (saveCounter >= SAVE_INTERVAL) {
            saveCounter = 0;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                VillageDataStorage.get(level).saveToFile(level);
            }
        }

        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkAndCollectTaxes(event.getServer());
        }
    }

    private static void checkAndCollectTaxes(net.minecraft.server.MinecraftServer server) {
        // Check all villages across all dimensions
        for (ServerLevel level : server.getAllLevels()) {
            ProtectionRadiusManager manager = ProtectionRadiusManager.getInstance();

            for (VillageData village : manager.getAllVillages().values()) {
                if (village.hasOwner() && village.isTaxDue()) {
                    boolean taxPaid = village.collectTax();

                    if (!taxPaid) {
                        // Insufficient funds - abandon village
                        SacredSettlements.LOGGER.info("Village at {} abandoned due to unpaid taxes",
                                village.getBellPosition());
                        village.abandonVillage();
                    } else {
                        SacredSettlements.LOGGER.info("Collected {} emeralds in taxes from village at {}",
                                village.calculateTaxOwed(), village.getBellPosition());
                    }
                }
            }
        }
    }
}