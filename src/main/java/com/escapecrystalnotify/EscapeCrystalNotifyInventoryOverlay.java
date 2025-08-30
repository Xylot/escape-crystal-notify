package com.escapecrystalnotify;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;

public class EscapeCrystalNotifyInventoryOverlay extends WidgetItemOverlay {
    private final EscapeCrystalNotifyPlugin plugin;
    private final EscapeCrystalNotifyConfig config;
    private final ItemManager itemManager;
    private final Cache<Long, Image> fillCache;

    @Inject
    EscapeCrystalNotifyInventoryOverlay(ItemManager itemManager, EscapeCrystalNotifyPlugin plugin, EscapeCrystalNotifyConfig config) {
        this.itemManager = itemManager;
        this.plugin = plugin;
        this.config = config;

        showOnInventory();
        showOnEquipment();

        fillCache = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(32).build();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        boolean atNotifyRegion;

        if (this.plugin.isAtNotifyRegionId()) {
            atNotifyRegion = true;
        } else {
            atNotifyRegion = this.config.alwaysDisplayInventory();
        }

        if (itemId != ItemID.ESCAPE_CRYSTAL) {
            return;
        }

        boolean shouldRenderMainDisplay = this.config.enableInventoryDisplay() && atNotifyRegion && this.plugin.isAccountTypeEnabled();
        
        boolean shouldRenderNonHardcoreHighlight = this.config.enableNonHardcoreInventoryHighlight() && 
            !this.plugin.isHardcoreAccountType() && 
            atNotifyRegion;

        if (!shouldRenderMainDisplay && !shouldRenderNonHardcoreHighlight) {
            return;
        }

        graphics.setFont(FontManager.getRunescapeSmallFont());
        final Rectangle bounds = widgetItem.getCanvasBounds();
        
        if (shouldRenderMainDisplay) {
            renderModelHighlight(graphics, bounds);
            renderCrystalModelSubtext(graphics, bounds);
            renderCrystalModelInfoText(graphics, bounds);
        } else if (shouldRenderNonHardcoreHighlight) {
            renderNonHardcoreModelHighlight(graphics, bounds);
        }
    }

    private void renderCrystalModelSubtext(Graphics2D graphics, Rectangle modelBounds) {
        final TextComponent textComponent = new TextComponent();

        textComponent.setPosition(new Point(modelBounds.x - 1, modelBounds.y + 35));

        if (plugin.isEscapeCrystalActive()) {
            textComponent.setText(config.inventoryActiveText());
            textComponent.setColor(config.inventoryActiveTextColor());
        } else {
            textComponent.setText(config.inventoryInactiveText());
            textComponent.setColor(config.inventoryInactiveTextColor());
        }

        textComponent.render(graphics);
    }

    private void renderModelHighlight(Graphics2D graphics, Rectangle modelBounds) {
        Color color;

        if (plugin.isEscapeCrystalActive()) {
            color = config.inventoryActiveFillColor();
        } else {
            color = config.inventoryInactiveFillColor();
        }

        switch (this.config.inventoryOverlayType()) {
            case ITEM_FILL: {
                Image image = getModelFillImage(ItemID.ESCAPE_CRYSTAL, 1, color);
                graphics.drawImage(image, modelBounds.x, modelBounds.y, null);
                break;
            }
            case BACKGROUND_FILL: {
                graphics.setColor(color);
                graphics.fill(modelBounds);
                break;
            }
            default:
        }
    }

    private void renderNonHardcoreModelHighlight(Graphics2D graphics, Rectangle modelBounds) {
        Color color;

        if (plugin.isEscapeCrystalActive()) {
            color = config.nonHardcoreInventoryActiveFillColor();
        } else {
            color = config.nonHardcoreInventoryInactiveFillColor();
        }

        switch (this.config.nonHardcoreInventoryOverlayType()) {
            case ITEM_FILL: {
                Image image = getModelFillImage(ItemID.ESCAPE_CRYSTAL, 1, color);
                graphics.drawImage(image, modelBounds.x, modelBounds.y, null);
                break;
            }
            case BACKGROUND_FILL: {
                graphics.setColor(color);
                graphics.fill(modelBounds);
                break;
            }
            default:
        }
    }

    private Image getModelFillImage(int itemId, int quantity, Color color) {
        long key = (((long) itemId) << 32) | color.getRGB() | color.getAlpha();
        Image image = fillCache.getIfPresent(key);
        if (image == null)
        {
            image = ImageUtil.fillImage(itemManager.getImage(itemId, quantity, false), color);
            fillCache.put(key, image);
        }
        return image;
    }

    private void renderCrystalModelInfoText(Graphics2D graphics, Rectangle modelBounds) {
        String infoText = this.plugin.getItemModelDisplayText(this.config.inventoryDisplayFormat(), this.config.inventoryInactivityTimeFormat(), this.config.inventoryTimeExpiredText());

        final TextComponent textComponent = new TextComponent();

        FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());

        int textWidth = metrics.stringWidth(infoText);
        int textAscent = metrics.getAscent();
        int textDescent = metrics.getDescent();

        int xDrawLocation = modelBounds.x + (int) (modelBounds.getWidth() - textWidth) / 2 - 2;
        int yDrawLocation = modelBounds.y +  (int) (modelBounds.getHeight() - (textAscent + (modelBounds.getHeight() - (textAscent + textDescent))) / 3);
        Point position;
        position = new Point(xDrawLocation, yDrawLocation);

        textComponent.setPosition(position);

        textComponent.setText(infoText);
        textComponent.setColor(this.plugin.getItemModelDisplayTextColor(this.config.inventoryDisplayFormat()));

        textComponent.render(graphics);

    }

}
