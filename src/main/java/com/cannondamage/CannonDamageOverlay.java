package com.cannondamage;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class CannonDamageOverlay extends OverlayPanel {

	private final Client client;
	private final CannonDamagePlugin plugin;

	@Inject
	public CannonDamageOverlay(Client client, CannonDamagePlugin plugin) {
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (!plugin.shouldShowOverlay()) {
			return null; // Only render when cannon is active
		}
		panelComponent.getChildren().add(LineComponent.builder()
				.left("Cannon Damage:")
				.right(String.valueOf(plugin.getTotalDamage()))
				.build());

		panelComponent.getChildren().add(LineComponent.builder()
				.left("Cannonballs Used:")
				.right(String.valueOf(plugin.getCannonballsUsed()))
				.build());

		panelComponent.getChildren().add(LineComponent.builder()
				.left("Avg Damage:")
				.right(String.format("%.3f", plugin.getAverageDamagePerCannonball()))
				.build());

		return super.render(graphics);
	}
}
