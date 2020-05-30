package fr.altaks.eodungeons.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import fr.altaks.eodungeons.Main;

/**
 * @author Altaks
 */
public class PlayerAttackMobListener implements Listener {

	// Stocke la classe Main du plugin dans une variable
	private Main main;
	
	// Constructeur qui permet d'attribuer la valeur à la variable privée main ci-dessus
	public PlayerAttackMobListener(Main main) {
		this.main = main;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamaged(EntityDamageEvent event) {
		Entity damagedEntity = event.getEntity();
		if (main.getActiveEntityIDs().contains(damagedEntity.getUniqueId())) { // Si l'entité a été spawnée par un donjon
			if (!(damagedEntity instanceof LivingEntity)) return;
			
			LivingEntity livingEntity = (LivingEntity) damagedEntity;
			
			if (event.getDamage() >= livingEntity.getHealth()) {
				
				main.getActiveEntityIDs().remove(damagedEntity.getUniqueId());
				
				if (main.getActiveBossBars().containsKey(damagedEntity.getUniqueId())) {
					main.getActiveBossBars().get(damagedEntity.getUniqueId()).removeAll();
					main.getActiveBossBars().remove(damagedEntity.getUniqueId());
				}
				
				damagedEntity.remove();
				return;
			} else {
				double life = livingEntity.getHealth(), maxlife = livingEntity.getMaxHealth();
				
				main.getActiveBossBars().get(damagedEntity.getUniqueId()).setProgress(life / maxlife);
				return;
			}

		}

	}

}
