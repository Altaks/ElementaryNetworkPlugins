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
	 * Fonction ex�cut�e � chaque d�connexion de joueur
	 * @param event -> Param�tre g�r� par Spigot
	 */
	@EventHandler
	public void onDisconnection(PlayerQuitEvent event) {
		Player player = event.getPlayer(); // On r�cup�re le joueur concern� par l'�vent
		Location loc = player.getLocation(); // On r�cup�re son endroit de d�connexion
		String disconnectionString = getDisconnectionLocString(loc); // On r�cup�re ses coordonn�es de d�connection en un String suivant un pattern sp�cifique
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()); // On r�cup�re puis on charge le fichier des endroits de deconnexions des joueurs
		yml.set(player.getUniqueId().toString(), disconnectionString); // On inscrit la position de d�connexion du joueur dans le fichier
		
		// On sauvegarde le fichier sur lequel on a �crit
		try {
			yml.save(main.getDisconnectionLocationsFile());
		} catch (IOException e) {
			// Si une erreur de type IOException (erreur de fichiers) se produit, alors �crire l'erreur dans la console
			e.printStackTrace();
		}
		
		if(main.getLinkedDongeon().containsKey(player.getUniqueId())) { // Si le joueur est dans un donjon (lors de sa d�connexion donc)
			Dongeon dj = main.getLinkedDongeon().get(player.getUniqueId()); // On r�cup�re le donjon dans lequel se trouve
			String str = getDisconnectionLocString(dj.getDungeonStartLocation()); // On r�cup�re la position de d�part du donjon sous forme de String suivant un pattern sp�cifique
			YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()).set(player.getUniqueId().toString(), str); // On charge le fichier qui contient les endroits de d�connexion
			
			// On sauvegarde le fichier sur lequel on a �crit
			try {
				yml.save(main.getDisconnectionLocationsFile());
			} catch (IOException e) {
				// Si une erreur de type IOException (erreur de fichiers) se produit, alors �crire l'erreur dans la console
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Fonction permettant de r�cuperer une Location en String avec un pattern particulier : "[world/x/y/z/pitch/yaw]"
	 * @param location -> Location qui va �tre r�-�crite en String
	 * @return String -> String qui suit le pattern pr�vu
	 */
	public String getDisconnectionLocString(Location location) {
		return "[" + location.getWorld().getName() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ() + "/" + location.getPitch() + "/" + location.getYaw() + "]";
	}
	
	/**
	 * Fonction permettant de r�cup�rer un Location depuis un String avec un pattern particulier : "[world/x/y/z/pitch/yaw]"
	 * @param str -> String qui contient la location 
	 * @return Location -> Location qui est �crite sous forme de String
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
	 * Fonction ex�cut�e � chaque connexion d'un joueur
	 * @param event -> Event g�r� par Spigot
	 */
	@EventHandler
	public void spawnAtDisconnectionLocs(PlayerJoinEvent event) {
		Player player = event.getPlayer(); // On r�cup�re le joueur
		Location location = readLocFromString(YamlConfiguration.loadConfiguration(main.getDisconnectionLocationsFile()).getString(player.getUniqueId().toString())); // On r�cup�re la position de d�connexion de ce joueur inscrite dans le fichier
		player.teleport(location); // On t�l�porte le joueur � cette position
	}

}
