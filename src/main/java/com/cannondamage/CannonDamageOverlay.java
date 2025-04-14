package com.cannondamage;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class CannonDamageOverlay extends OverlayPanel
{
    private final CannonDamagePlugin plugin;

    @Inject
    public CannonDamageOverlay(CannonDamagePlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isPluginEnabled())
        {
            return null;
        }

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Cannon Damage:")
                .right(String.valueOf(plugin.getTotalDamage()))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Cannonballs Used:")
                .right(String.valueOf(plugin.getCannonballsUsed()))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Avg Dmg/Ball:")
                .right(String.format("%.2f", plugin.getAverageDamagePerCannonball()))
                .build());

        return super.render(graphics);
    }
}
