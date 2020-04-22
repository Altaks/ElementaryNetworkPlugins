package fr.altaks.eodungeons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import fr.altaks.eodungeons.commands.StopDungeonCommand;
import fr.altaks.eodungeons.core.Dongeon;
import fr.altaks.eodungeons.listener.ConnectionListener;
import fr.altaks.eodungeons.listener.InteractListener;
import fr.altaks.eodungeons.listener.PlayerAttackMobListener;

/**
 * Classe Main du plugin
 * @author Altaks
 */
public class Main extends JavaPlugin { // Il s'agit d'une classe Main qui hérite de JavaPlugin.
	
	private final List<Dongeon> activeDongeons = new ArrayList<>(); // Liste des donjons lancés
	private final List<UUID> activeEntityIDs = new ArrayList<>(); // Listes des entités de donjons (pour les actualisations de barres de vie)
	
	private final HashMap<UUID, Dongeon> linkedDongeon = new HashMap<>(); // Liaisons Joueur -> Donjon
	
	private final HashMap<UUID, Dongeon> spawnedBossesIDs = new HashMap<>(); // Liaisons Entité (étant un boss ou miniboss) -> Donjon
	private final HashMap<String, Dongeon> activeDungeons = new HashMap<>(); // Liaisons Nom de donjon -> Donjon
	
	public static final String PLUGIN_PREFIX = "§7[§eEODungeons§7] §6\u00BB "; // Préfixe de tchat du plugin
	
	// Dossiers des listes de mobs, listes de loots, et fichier de sauvegarde des endroits de déconnection
	private File waveDirectory, lootsDirectory, disconnectionLocsFile;
	
	public static boolean isDebugging = true;
	
	@Override
	public void onEnable() { // Voici la fonction qui se lance à chaque démarrage / reload d'un serveur
		
		// Vérifie si le dossier du plugin existe. Si non -> création
		if(!getDataFolder().exists()) getDataFolder().mkdir();
		
		// Vérifie si le dossier des listes existe. Si non -> création
		waveDirectory = new File(getDataFolder() + File.separator + "lists/");
		if(!waveDirectory.exists()) waveDirectory.mkdir();
		
		// Vérifie si le dossier des listes de loots existe. Si non -> création
		lootsDirectory = new File(getDataFolder() + File.separator + "loots/");
		if(!lootsDirectory.exists()) lootsDirectory.mkdir();
		
		// Vérifie si le fichier des endroits de déconnection existe. Si non -> création avec risque de IOExeption
		disconnectionLocsFile = new File(getDataFolder() + File.separator + "disconnection_locations.yml");
		if(!disconnectionLocsFile.exists()) {
			try {
				disconnectionLocsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Enregistrement sur le serveur en tant que Listeners les classes InteractListener, PlayerAttackMobListener et ConnectionListener
		 */
		getServer().getPluginManager().registerEvents(new InteractListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerAttackMobListener(this), this);
		getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
		
		/*
		 * Activation de la commande stopcommand
		 */
		getCommand("stopdungeon").setExecutor(new StopDungeonCommand(this));
		getCommand("stopdungeon").setTabCompleter(new StopDungeonCommand(this));
	}
	
	/*
	 * Permet d'obtenir le File (fichier) qui contient les endroits de déconnections
	 */
	public File getDisconnectionLocationsFile() {
		return this.disconnectionLocsFile;
	}
	
	/*
	 * Permet d'obtenir le File (dossier) qui contient les listes de mobs
	 */
	public File getWaveDirectory() {
		return this.waveDirectory;
	}
	
	/*
	 * Permet d'obtenir le File (dossier) qui contient les listes deloots
	 */
	public File getLootsDirectory() {
		return this.lootsDirectory;
	}
	
	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(player -> { // pour chaque joueur
			if(this.linkedDongeon.containsKey(player.getUniqueId())) { 
				/*
				 * Si le joueur est dans un donjon -> teleporter le joueur à l'entrée du donjon
				 */
				player.teleport(this.linkedDongeon.get(player.getUniqueId()).getDungeonStartLocation());
				/*
				 * Retirer le link entre le donjon et le joueur
				 */
				this.linkedDongeon.remove(player.getUniqueId());
			}
		});
		
		this.activeEntityIDs.forEach(entityUUID -> { // pour chaque entité de donjon vivante
			((LivingEntity) Bukkit.getEntity(entityUUID)).setHealth(0.0d); // mettre la vie de cette entité à 0.0d
			this.activeEntityIDs.remove(entityUUID); // Retirer cette entité de la liste en cas de bug
		});
		
		this.spawnedBossesIDs.keySet().forEach(bossUUID -> { // pour chaque boss spawné
			((LivingEntity) Bukkit.getEntity(bossUUID)).setHealth(0.0d); // mettre la vie de ce boss ) 0.0d
			this.spawnedBossesIDs.remove(bossUUID); // retirer ce boss de la liste en cas de bug
		});
		
		this.activeDongeons.forEach(dongeon -> { // pour chaque donjon lancé
			dongeon.stop_loosing(true); // stopper le donjon en mode échec des joueurs
		});
	}
	
	// Permet de récupérer la liste des donjons actifs/lancés
	public List<Dongeon> getActiveDongeons(){
		return this.activeDongeons;
	}
	
	// Permet de récupérer la HashMap des UUIDs joueurs -> donjons associés
	public HashMap<UUID, Dongeon> getLinkedDongeon(){
		return this.linkedDongeon;
	}
	
	// Permet de récupérer la liste des mobs spawnés par les donjons
	public List<UUID> getActiveEntityIDs(){
		return this.activeEntityIDs;
	}
	
	// Permet d'obtenir la HashMap des UUIDs de boss -> donjons associés
	public HashMap<UUID, Dongeon> getSpawnedBossesIDs(){
		return this.spawnedBossesIDs;
	}
	
	// Permet d'obtenir la HashMap des String (noms) des donjons -> donjons associés
	public HashMap<String, Dongeon> getActiveDungeons(){
		return this.activeDungeons;
	}
	

}
