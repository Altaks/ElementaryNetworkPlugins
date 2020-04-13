package fr.altaks.eodungeons.listener;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.core.Dongeon;

/**
 * @author Altaks
 */
public class ConnectionListener implements Listener {
	
	/**
	 * Variable main qui contient le main du plugin
	 */
	private Main main;
	
	/**
	 * Constructeur qui va attribuer le main a la variable ci-dessus
	 * @param main -> Classe main du plugin
	 */
	public ConnectionListener(Main main) {
		this.main = main;
	}
	
	/**
	 * Fonction exécutée à chaque déconnexion de joueur
	 * @param event -> Paramètre géré par Spigot
	 */
	@EventHandler
	public void onDisconnection(PlayerQuitEvent event) {
		Player player = event.getPlayer(); // On récupère le joueur concerné par l'évent
		Location loc = player.getLocation(); // On récupère son endroit de déconnexion
		String disconnectionString = getDisconnectionLocString(loc); // On récupère ses coordonnées de déconnection en un String suivant un pattern spécifique
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()); // On récupère puis on charge le fichier des endroits de deconnexions des joueurs
		yml.set(player.getUniqueId().toString(), disconnectionString); // On inscrit la position de déconnexion du joueur dans le fichier
		
		// On sauvegarde le fichier sur lequel on a écrit
		try {
			yml.save(main.getDisconnectionLocationsFile());
		} catch (IOException e) {
			// Si une erreur de type IOException (erreur de fichiers) se produit, alors écrire l'erreur dans la console
			e.printStackTrace();
		}
		
		if(main.getLinkedDongeon().containsKey(player.getUniqueId())) { // Si le joueur est dans un donjon (lors de sa déconnexion donc)
			Dongeon dj = main.getLinkedDongeon().get(player.getUniqueId()); // On récupère le donjon dans lequel se trouve
			String str = getDisconnectionLocString(dj.getDungeonStartLocation()); // On récupère la position de départ du donjon sous forme de String suivant un pattern spécifique
			YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()).set(player.getUniqueId().toString(), str); // On charge le fichier qui contient les endroits de déconnexion
			
			// On sauvegarde le fichier sur lequel on a écrit
			try {
				yml.save(main.getDisconnectionLocationsFile());
			} catch (IOException e) {
				// Si une erreur de type IOException (erreur de fichiers) se produit, alors écrire l'erreur dans la console
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Fonction permettant de récuperer une Location en String avec un pattern particulier : "[world/x/y/z/pitch/yaw]"
	 * @param location -> Location qui va être ré-écrite en String
	 * @return String -> String qui suit le pattern prévu
	 */
	public String getDisconnectionLocString(Location location) {
		return "[" + location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ() + "/" + location.getPitch() + "/" + location.getYaw() + "]";
	}
	
	/**
	 * Fonction permettant de récupérer un Location depuis un String avec un pattern particulier : "[world/x/y/z/pitch/yaw]"
	 * @param str -> String qui contient la location 
	 * @return Location -> Location qui est écrite sous forme de String
	 */
	public Location readLocFromString(String str) {
		str = str.replace("[", "").replace("]", ""); // On retire les "[" et "]" qui servent de marqueurs "human-friendly" et "YAML-friendly"
		return new Location(Bukkit.getWorld(str.split("/")[0]), 
								Double.parseDouble(str.split("/")[1]), 
								Double.parseDouble(str.split("/")[2]), 
								Double.parseDouble(str.split("/")[3]),
								Float.parseFloat(str.split("/")[5]),
								Float.parseFloat(str.split("/")[4]));
		
	}
	
	/**
	 * Fonction exécutée à chaque connexion d'un joueur
	 * @param event -> Event géré par Spigot
	 */
	@EventHandler
	public void spawnAtDisconnectionLocs(PlayerJoinEvent event) {
		Player player = event.getPlayer(); // On récupère le joueur
		Location location = readLocFromString(YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()).getString(player.getUniqueId().toString())); // On récupère la position de déconnexion de ce joueur inscrite dans le fichier
		player.teleport(location); // On téléporte le joueur à cette position
	}

}
