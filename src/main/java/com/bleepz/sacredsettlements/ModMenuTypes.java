package com.bleepz.sacredsettlements;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SacredSettlements.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VillageBellMenu>> VILLAGE_BELL_MENU =
            MENUS.register("village_bell_menu",
                    () -> IMenuTypeExtension.create(VillageBellMenu::new));
}
