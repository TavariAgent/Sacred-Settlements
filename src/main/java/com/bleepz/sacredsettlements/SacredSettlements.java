package com.bleepz.sacredsettlements;

import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SacredSettlements.MODID)
public class SacredSettlements {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sacredsettlements";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "sacredsettlements" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "sacredsettlements" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "sacredsettlements" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SacredSettlements(IEventBus modEventBus, ModContainer modContainer) {

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so menus get registered
        ModMenuTypes.MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (SacredSettlements) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register tax collection system
        NeoForge.EVENT_BUS.register(TaxCollectionSystem.class);

        // Register our protection event handler
        NeoForge.EVENT_BUS.register(new ProtectionEventHandler());

        // Register our bell interaction handler
        NeoForge.EVENT_BUS.register(new BellInteractionHandler());

        modContainer.registerConfig(ModConfig.Type.COMMON, ProtectionConfig.SPEC, "sacredsettlements-protection.toml");
    }

    @SubscribeEvent
    public void onCommandRegister(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        UnstuckCommand.register(event.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        // Set the overworld as the storage level
        ServerLevel overworld = event.getServer().overworld();
        ProtectionRadiusManager.getInstance().setLevel(overworld);

        // Load village data
        VillageDataStorage.get(overworld);

        LOGGER.info("Sacred Settlements loaded village data");

    }

    @SubscribeEvent
    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        // Save village data when server stops
        ServerLevel overworld = event.getServer().overworld();
        VillageDataStorage storage = VillageDataStorage.get(overworld);
        storage.saveToFile(overworld);
        VillageDataStorage.reset();

        LOGGER.info("Sacred Settlements saved village data");
    }
}
