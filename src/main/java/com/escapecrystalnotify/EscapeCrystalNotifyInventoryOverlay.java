package com.escapecrystalnotify;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.runelite.api.gameval.ItemID;
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
    private final Cache<Integer, Image> fillCache;
    private final TextComponent textComponent = new TextComponent();

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
        if (itemId != ItemID.TOB_TELEPORT) {
            return;
        }

        boolean atNotifyRegion = plugin.isAtNotifyRegionId() || config.alwaysDisplayInventory();
        if (!atNotifyRegion) return;

        boolean shouldRenderMainDisplay = config.enableInventoryDisplay() && plugin.isAccountTypeEnabled();
        
        boolean shouldRenderNonHardcoreHighlight = !shouldRenderMainDisplay &&
            config.enableNonHardcoreInventoryHighlight() && !plugin.isHardcoreAccountType();

        if (!shouldRenderMainDisplay && !shouldRenderNonHardcoreHighlight) {
            return;
        }

        final Rectangle bounds = widgetItem.getCanvasBounds();
        
        if (shouldRenderMainDisplay) {
            graphics.setFont(FontManager.getRunescapeSmallFont());
            renderHighlight(graphics, bounds, config.inventoryOverlayType(), plugin.isEscapeCrystalActive()
                ? config.inventoryActiveFillColor() : config.inventoryInactiveFillColor());
            renderCrystalModelSubtext(graphics, bounds);
            renderCrystalModelInfoText(graphics, bounds);
        } else if (shouldRenderNonHardcoreHighlight) {
            renderHighlight(graphics, bounds, config.nonHardcoreInventoryOverlayType(), plugin.isEscapeCrystalActive()
                ? config.nonHardcoreInventoryActiveFillColor() : config.nonHardcoreInventoryInactiveFillColor());
        }
    }

    private void renderCrystalModelSubtext(Graphics2D graphics, Rectangle modelBounds) {
        boolean active = plugin.isEscapeCrystalActive();
        String text = active ? config.inventoryActiveText() : config.inventoryInactiveText();
        if (text.isEmpty()) return;
        renderText(graphics, text, active ? config.inventoryActiveTextColor() : config.inventoryInactiveTextColor(),
            modelBounds.x - 1, modelBounds.y + 35);
    }

    private void renderHighlight(Graphics2D graphics, Rectangle modelBounds,
        EscapeCrystalNotifyConfig.ModelOverlayType type, Color color) {
        if (type == EscapeCrystalNotifyConfig.ModelOverlayType.DISABLED || color.getAlpha() == 0) return;
        switch (type) {
            case ITEM_FILL: {
                Image image = getCrystalFillImage(color);
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

    Image getCrystalFillImage(Color color) {
        int key = color.getRGB();
        Image image = fillCache.getIfPresent(key);
        if (image == null)
        {
            image = ImageUtil.fillImage(itemManager.getImage(ItemID.TOB_TELEPORT, 1, false), color);
            fillCache.put(key, image);
        }
        return image;
    }

    private void renderCrystalModelInfoText(Graphics2D graphics, Rectangle modelBounds) {
        EscapeCrystalNotifyConfig.OverlayDisplayType format = config.inventoryDisplayFormat();
        String infoText = plugin.getItemModelDisplayText(format, config.inventoryInactivityTimeFormat(), config.inventoryTimeExpiredText());
        if (infoText.isEmpty()) return;
        Color color = plugin.getItemModelDisplayTextColor(format);
        if (color.getAlpha() == 0) return;

        FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());

        int textWidth = metrics.stringWidth(infoText);
        int textAscent = metrics.getAscent();
        int textDescent = metrics.getDescent();

        int xDrawLocation = modelBounds.x + (int) (modelBounds.getWidth() - textWidth) / 2 - 2;
        int yDrawLocation = modelBounds.y +  (int) (modelBounds.getHeight() - (textAscent + (modelBounds.getHeight() - (textAscent + textDescent))) / 3);
        renderText(graphics, infoText, color, xDrawLocation, yDrawLocation);
    }

    private void renderText(Graphics2D graphics, String text, Color color, int x, int y) {
        if (color.getAlpha() == 0) return;
        textComponent.setPosition(x, y);
        textComponent.setText(text);
        textComponent.setColor(color);
        textComponent.render(graphics);
    }
}
