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
	
	// Constructeur qui permet d'attribuer la valeur � la variable priv�e main ci-dessus
	public PlayerAttackMobListener(Main main) {
		this.main = main;
	}
	
	/**
	 * Fonction qui va agir lorsqu'une entit� va en endommager une autre
	 * @param event -> G�r� par Spigot
	 */
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		Entity damagedEntity = event.getEntity(); // On stocke l'entit� endommag�e dans une variable
		
		if(!(damagedEntity instanceof LivingEntity)) return; // Si cette entit� n'est pas "vivante" alors un arr�te la lecture de la fonction
		
		if(main.getActiveEntityIDs().contains(damagedEntity.getUniqueId())) { // Si cette entit�e � �t� spawn�e par un donjon alors
			if(event.getDamage() >= ((LivingEntity)damagedEntity).getHealth()) { // Si les d�gats sont sup�rieurs � la vie de l'entit� alors
				main.getActiveEntityIDs().remove(damagedEntity.getUniqueId()); // retirer l'entit� de la liste des mobs spawn�es �tant donn� qu'elle va mourir
				return; // Arr�ter la lecture de la fonction
			}
			
			if(event.getDamager() instanceof Player) { // Si l'entit� qui a endommag� est un joueur alors
				
				if(main.getSpawnedBossesIDs().containsKey(damagedEntity.getUniqueId())) { // Si l'entit�e endommag�e est un boss/mini-boss alors 
					List<Player> activePlayers = main.getSpawnedBossesIDs().get(damagedEntity.getUniqueId()).getActivePlayers(); // On r�cup�re la liste des joueurs li�s au m�me donjon que le boss
					LivingEntity livingDamagedEntity = (LivingEntity) damagedEntity; // On re-r�cup�re l'entit� endommag�e mais en tant qu'entit� vivante
					
					@SuppressWarnings("deprecation")
					double entityCurrentHealth = livingDamagedEntity.getHealth() , entityMaxHealth = livingDamagedEntity.getMaxHealth(); // On r�cup�re ses donn�es de sant�

					TextComponent life = StringUtil.getTextComponent(entityCurrentHealth, entityMaxHealth, 25, '|'); // On forme la barre de vie du mob qui sera visible en ActionBar par les joueurs
					
					activePlayers.forEach(player -> { // pour chaque joueur du donjon
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, life); // on envoie la barre de vie en ActionBars
					});
				}
			}
		}
	}

}
