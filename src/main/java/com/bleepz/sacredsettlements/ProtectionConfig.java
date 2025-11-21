package com.bleepz.sacredsettlements;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ProtectionConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue PROTECTION_RADIUS;
    public static final ModConfigSpec.DoubleValue TAX_RATE_PER_HOUR;
    public static final ModConfigSpec.IntValue TAX_COLLECTION_HOURS;
    public static final ModConfigSpec.IntValue VILLAGE_PURCHASE_PRICE;

    static {
        BUILDER.push("Village Protection Settings");

        PROTECTION_RADIUS = BUILDER
                .comment("Radius in blocks around village bells that will be protected")
                .defineInRange("protectionRadius", 64, 16, 500);

        TAX_RATE_PER_HOUR = BUILDER
                .comment("Emeralds deducted per hour from village treasury")
                .defineInRange("taxRatePerHour", 0.5, 0.0, 10.0);

        TAX_COLLECTION_HOURS = BUILDER
                .comment("Hours between tax collections (default: 12 hours)")
                .defineInRange("taxCollectionHours", 12, 1, 168); // Max 1 week

        VILLAGE_PURCHASE_PRICE = BUILDER
                .comment("Number of emeralds required to purchase a village")
                .defineInRange("villagePurchasePrice", 64, 1, 2304); // Max = 36 stacks

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}