package com.cannondamage;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;

@PluginDescriptor(
		name = "Cannon Damage",
		description = "Tracks the amount of damage done from the cannon and the number of cannonballs used.",
		tags = {"cannon", "damage", "tracker"}
)
@Slf4j
public class CannonDamagePlugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CannonDamageOverlay cannonDamageOverlay;

	// Cannon-related constants
	private static final Set<Integer> CANNON_GAME_OBJECT_IDS = Set.of(6, 43027); // Replace with actual IDs
	private static final Set<Integer> CANNONBALL_PROJECTILE_IDS = Set.of(53, 2018); // Allow multiple projectile IDs
	private static final double XP_PER_DAMAGE = 2.0; // Cannon deals 2 ranged XP per 1 damage
	private static final long OVERLAY_DISPLAY_DURATION = 60_000; // Duration to keep showing overlay after cannon is removed

	// Cannon-related state
	private final Queue<Projectile> trackedProjectiles = new LinkedList<>();
	private boolean cannonIsPlaced = false;

	// Damage and usage tracking
	private int totalDamage = 0;          // Tracks total cannon damage
	private int cannonballsUsed = 0;     // Tracks cannonballs used

	// Ranged XP tracking
	private double cannonStartRangedXp = 0; // Ranged XP at the moment the cannon is placed

	// Overlay-related state
	private boolean showOverlay = false; // Tracks visibility of the overlay
	private long hideOverlayTime = 0;    // Tracks when to stop showing overlay

	@Override
	protected void startUp() {
		log.info("Cannon Damage Plugin started.");
		overlayManager.add(cannonDamageOverlay); // Add the overlay to the manager
	}

	@Override
	protected void shutDown() {
		log.info("Cannon Damage Plugin stopped.");
		overlayManager.remove(cannonDamageOverlay); // Remove the overlay
		resetTracker(); // Reset all tracked values
	}

	private void resetTracker() {
		totalDamage = 0;
		cannonballsUsed = 0;
		cannonIsPlaced = false;
		showOverlay = false;
		trackedProjectiles.clear();
		cannonStartRangedXp = client.getSkillExperience(Skill.RANGED); // Reset XP tracking
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		GameObject spawnedObject = event.getGameObject();

		// If a cannon is placed, enable the overlay
		if (CANNON_GAME_OBJECT_IDS.contains(spawnedObject.getId())) {
			cannonIsPlaced = true;
			showOverlay = true;

			// Record the player's current Ranged XP as the starting point
			cannonStartRangedXp = client.getSkillExperience(Skill.RANGED);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		GameObject despawnedObject = event.getGameObject();

		// If the cannon is despawned, stop tracking Ranged XP
		if (CANNON_GAME_OBJECT_IDS.contains(despawnedObject.getId())) {
			cannonIsPlaced = false;
			hideOverlayTime = System.currentTimeMillis() + OVERLAY_DISPLAY_DURATION;
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event) {
	    Projectile projectile = event.getProjectile();
	
	    // Only track valid cannon projectiles
	    if (CANNONBALL_PROJECTILE_IDS.contains(projectile.getId()) && !trackedProjectiles.contains(projectile)) {
	        // If we already have 5 projectiles, remove the oldest one
	        if (trackedProjectiles.size() >= 5) {
	            trackedProjectiles.poll(); // Removes and returns the head (oldest projectile)
	        }
	
	        trackedProjectiles.add(projectile); // Add the new projectile
	        cannonballsUsed++;
	    }
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (!cannonIsPlaced) {
			return; // Skip tracking if the cannon is not active
		}

		// Calculate the player's Ranged XP gained since the cannon was placed
		double currentRangedXp = client.getSkillExperience(Skill.RANGED);
		double xpGained = currentRangedXp - cannonStartRangedXp;

		// Only consider cannon damage
		if (xpGained > 0) {
			int damageDealt = (int) Math.round(xpGained / XP_PER_DAMAGE);
			totalDamage = damageDealt; // Update the total damage based on XP tracked

			// Print updates to the console
		}
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public int getCannonballsUsed() {
		return cannonballsUsed;
	}

	public double getAverageDamagePerCannonball() {
		return (cannonballsUsed > 0) ? (double) totalDamage / cannonballsUsed : 0.0;
	}

	public boolean shouldShowOverlay() {
		// Show overlay while cannon is active or within the grace period after cannon removal
		return showOverlay && (cannonIsPlaced || System.currentTimeMillis() < hideOverlayTime);
	}
}
