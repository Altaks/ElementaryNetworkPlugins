package fr.altaks.eodungeons.util;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class DungeonPresentationUtil {
	
	public static HashMap<String, DungeonInfos> presentations = new HashMap<String, DungeonInfos>();
	{
		
	}
	
	public class DungeonInfos {
		
		public String text;
		public int level, recommandedGroupSizeMin, recommandedGroupSizeMax;
		
		public DungeonInfos(int level, int recommandedGroupSizeMin, int recommandedGroupSizeMax, String text) {
			this.level = level;
			this.recommandedGroupSizeMax = recommandedGroupSizeMax;
			this.recommandedGroupSizeMin = recommandedGroupSizeMin;
			this.text = text;
		}
		
	}
	
	public static void spawnLoreHologram(Location location, String...text) {
		for(int i = 0; i < text.length; i++) spawnHologram(location.add(0, (i/5), 0), text[i]); return;
	}
	
	public static void spawnStatsHologram(Location location, int level, int recommandedGroupSizeMin, int recommandedGroupSizeMax) {
		
		String groupSize = (recommandedGroupSizeMax == recommandedGroupSizeMin) ? recommandedGroupSizeMin + "" : recommandedGroupSizeMin + "-" + recommandedGroupSizeMax;
		
		spawnHologram(location, "Recommandé : " + groupSize + " joeurs");
		
		location = location.add(0,0.25,0);
		
		spawnHologram(location, "Donjon de niv : " + level);
		return;
		
	}
	
	@SuppressWarnings("deprecation")
	private static void spawnHologram(Location loc, String name) {
		
		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		
		stand.setInvulnerable(true);
		
		stand.setCustomName(name);
		stand.setCustomNameVisible(true);
		
		stand.setGravity(false);
		stand.setCollidable(false);
		stand.setSilent(true);
		
		stand.setMaxHealth(1d);
		stand.setHealth(1d);
		return;
	}

}
