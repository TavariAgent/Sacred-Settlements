package com.bleepz.sacredsettlements;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DynamicButton extends Button {
    private final Supplier<Component> messageSupplier;

    public DynamicButton(int x, int y, int width, int height,
                         Supplier<Component> messageSupplier, OnPress onPress) {
        super(x, y, width, height, messageSupplier.get(), onPress, DEFAULT_NARRATION);
        this.messageSupplier = messageSupplier;
    }

    @Override
    public @NotNull Component getMessage() {
        // Update message dynamically every frame
        return messageSupplier.get();
    }
}
