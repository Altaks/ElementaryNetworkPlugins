package fr.altaks.eodungeons.listener;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.util.StringUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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
	
	/**
	 * Fonction qui va agir lorsqu'une entité va en endommager une autre
	 * @param event -> Géré par Spigot
	 */
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		Entity damagedEntity = event.getEntity(); // On stocke l'entité endommagée dans une variable
		
		if(!(damagedEntity instanceof LivingEntity)) return; // Si cette entité n'est pas "vivante" alors un arrête la lecture de la fonction
		
		if(main.getActiveEntityIDs().contains(damagedEntity.getUniqueId())) { // Si cette entitée à été spawnée par un donjon alors
			if(event.getDamage() >= ((LivingEntity)damagedEntity).getHealth()) { // Si les dégats sont supérieurs à la vie de l'entité alors
				main.getActiveEntityIDs().remove(damagedEntity.getUniqueId()); // retirer l'entité de la liste des mobs spawnées étant donné qu'elle va mourir
				return; // Arrêter la lecture de la fonction
			}
			
			if(event.getDamager() instanceof Player) { // Si l'entité qui a endommagé est un joueur alors
				
				if(main.getSpawnedBossesIDs().containsKey(damagedEntity.getUniqueId())) { // Si l'entitée endommagée est un boss/mini-boss alors 
					List<Player> activePlayers = main.getSpawnedBossesIDs().get(damagedEntity.getUniqueId()).getActivePlayers(); // On récupère la liste des joueurs liés au même donjon que le boss
					LivingEntity livingDamagedEntity = (LivingEntity) damagedEntity; // On re-récupère l'entité endommagée mais en tant qu'entité vivante
					
					@SuppressWarnings("deprecation")
					double entityCurrentHealth = livingDamagedEntity.getHealth() , entityMaxHealth = livingDamagedEntity.getMaxHealth(); // On récupère ses données de santé

					TextComponent life = StringUtil.getTextComponent(entityCurrentHealth, entityMaxHealth, 25, '|'); // On forme la barre de vie du mob qui sera visible en ActionBar par les joueurs
					
					activePlayers.forEach(player -> { // pour chaque joueur du donjon
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, life); // on envoie la barre de vie en ActionBars
					});
				}
			}
		}
	}

}
