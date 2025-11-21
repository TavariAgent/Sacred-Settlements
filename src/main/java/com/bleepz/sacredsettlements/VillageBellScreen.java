package com.bleepz.sacredsettlements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class VillageBellScreen extends AbstractContainerScreen<VillageBellMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SacredSettlements.MODID, "textures/gui/village_bell.png");

    public VillageBellScreen(VillageBellMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Purchase button (only show if unclaimed)
        if (!this.menu.hasOwner()) {
            int price = ProtectionConfig.VILLAGE_PURCHASE_PRICE.get();
            Button purchaseButton = Button.builder(
                            Component.literal("Buy: " + price),
                            button -> onPurchaseClicked()
                    )
                    .bounds(x + 8, y + 20, 48, 50)
                    .build();

            this.addRenderableWidget(purchaseButton);
        } else {
            // Stats buttons - positioned right of the 3x3 emerald grid
            int statsX = x + 120;
            int statsY = y + 17;
            int buttonWidth = 52;
            int buttonHeight = 16;

            // Owner button (static)
            this.addRenderableWidget(
                    Button.builder(Component.literal("§6" + this.menu.getOwnerName()), b -> {})
                            .bounds(statsX, statsY, buttonWidth, buttonHeight)
                            .build()
            );

            // Ownership duration (dynamic - updates every frame)
            this.addRenderableWidget(
                    new DynamicButton(statsX, statsY + 19, buttonWidth, buttonHeight,
                            () -> Component.literal("§7" + this.menu.getOwnershipDurationFormatted()),
                            b -> {}
                    )
            );

            // Treasury (dynamic - shows emeralds and tax rate)
            this.addRenderableWidget(
                    new DynamicButton(statsX, statsY + 38, buttonWidth, buttonHeight,
                            () -> {
                                int emeralds = this.menu.getTotalEmeralds();

                                // Calculate actual tax rate from config
                                double ratePerHour = ProtectionConfig.TAX_RATE_PER_HOUR.get();
                                int collectionHours = ProtectionConfig.TAX_COLLECTION_HOURS.get();
                                double totalTax = ratePerHour * collectionHours;

                                // Format the display
                                String taxDisplay;
                                if (ratePerHour == 0) {
                                    taxDisplay = ""; // No tax
                                } else if (ratePerHour < 1.0) {
                                    taxDisplay = String.format(" §7-%.1f/hr", ratePerHour);
                                } else {
                                    taxDisplay = String.format(" §7-%.0f/hr", ratePerHour);
                                }

                                return Component.literal("§a" + emeralds + taxDisplay);
                            },
                            b -> {}
                    )
            );
        }
    }

    private void onPurchaseClicked() {
        // Send packet to server using the connection
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new PurchaseVillagePacket(this.menu.getBellPosition()));
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Simple blit - draws the full texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
