package fr.altaks.eodungeons.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.util.LootsUtil;

/**
 * @author Altaks
 */
public class Dongeon implements Listener {
	
	private final List<Player> activePlayers = new ArrayList<Player>(); // Liste des joueurs encore actifs dans le donjon
	private final List<UUID> beginningPlayers = new ArrayList<UUID>(); // Liste des joueurs actifs au départ du donjon
	
	private final HashMap<Location, Wave> waveActivationLocations = new HashMap<Location, Wave>(); // HashMap des Locations de panneaux -> vagues
	private Iterator<Entry<Location, Wave>> currentIteration; // Itérateur de l'HashMap juste au dessus
	private Entry<Location, Wave> currentWave; // Entrée actuelle de l'itérateur
	
	private Radius currentRadius; // Radius actuel en analyse
	private Location currentLocation; // Localisation actuelle en analyse
	
	private DongeonArea area; // Zone du donjon
	private String dungeonName = UUID.randomUUID().toString(); // Nom du donjon, défini par défaut sur une UUID random
	private DungeonLoots loots; // Loots du donjon
	private Location dongeonActivationLocation; // Localisation du départ du serveur
	
	private Main main; // Classe Main du plugin
	
	/**
	 * Constructeur de la classe Donjon ({@link fr.altaks.eodungeons.core.Dongeon})
	 * @param main -> Classe main du plugin
	 * @param dongeonActivationLocation -> Location de départ du donjon
	 * @param dungeonName -> Nom du donjon
	 * @param area -> Zone du donjon
	 * @param players -> Joueurs de départ du donjon
	 */
	public Dongeon(Main main, Location dongeonActivationLocation, String dungeonName, DongeonArea area, Player...players) {
		this.main = main;
		this.dungeonName = dungeonName;
		this.area = area;
		Arrays.asList(players).forEach(player -> { // Pour chaque joueur de la liste des joueurs de départ
			activePlayers.add(player); // On ajoute ce joueur aux joueurs actifs
			beginningPlayers.add(player.getUniqueId()); // On ajoute son UUID aux UUIDs des joueurs du départ
		});
		this.currentIteration = waveActivationLocations.entrySet().iterator(); // On définit l'itérateur
		this.dongeonActivationLocation = dongeonActivationLocation;
	}
	
	/**
	 * Constructeur de la classe Donjon ({@link fr.altaks.eodungeons.core.Dongeon})
	 * @param main -> Classe main du plugin
	 * @param dongeonActivationLocation -> Location de départ du donjon
	 * @param dungeonName -> Nom du donjon
	 * @param area -> Zone du donjon
	 * @param players -> Joueurs de départ du donjon
	 */
	public Dongeon(Main main, Location dongeonActivationLocation, String dungeonName, DongeonArea area, Collection<Player> players) {
		this.main = main;
		this.dungeonName = dungeonName;
		this.area = area;
		players.forEach(player -> { // Pour chaque joueur de la liste des joueurs de départ
			activePlayers.add(player); // On ajoute ce joueur aux joueurs actifs
			beginningPlayers.add(player.getUniqueId()); // On ajoute son UUID aux UUIDs des joueurs du départ
		});
		this.currentIteration = waveActivationLocations.entrySet().iterator(); // On définit l'itérateur
		this.dongeonActivationLocation = dongeonActivationLocation;
	}
	
	/**
	 * Fonction pour lancer un donjon
	 */
	@SuppressWarnings("deprecation")
	public void start() {
		if(main.getActiveDongeons().contains(this)) return; // Si le donjon est déja actif, arrêter de lire la fonction.
		
		Sign[] systemSigns = area.getSystemSignsFromArea(); // On récupère les panneaux de la zone du donjon, sous forme de tableau déja trié
		for(Sign sign : systemSigns) { // pour chaque panneau du tableau
			try {
				// on ajoute à la HashMap l'emplacement du panneau avec la vague (importée depuis un fichier)
				waveActivationLocations.put(sign.getLocation(), Wave.loadWaveFromYmlFile(this, main, new File(main.getWaveDirectory() + sign.getLines()[1].split(":")[1] + ".yml")));
			} catch (NullPointerException e) {
				// Si une NullPointerException se produit, alors on prévient la console via le Logger de Bukkit, puis on écrit l'erreur dans la console
				Bukkit.getLogger().warning("LISTE \""+sign.getLines()[1].split(":")[1]+"\" NON TROUVEE");
				e.printStackTrace();
			} catch (IOException e) {
				// Si une IOException se produit, alors on prévient la console via le logger de Bukkit, puis on écrit l'erreur dans la console
				Bukkit.getLogger().warning("LISTE \""+sign.getLines()[1].split(":")[1]+"\" NON TROUVEE");
				e.printStackTrace();
			}
		}
		
		
		// On récupère les fichiers des listes de loots
		File winLoots = new File(main.getLootsDirectory() + File.separator + dungeonName + "_win.yml");
		File failLoots = new File(main.getLootsDirectory() + File.separator + dungeonName + "_loose.yml");
		
		// On affecte les Loots du donjon avec les loots lus sur les deux fichiers
		this.loots = LootsUtil.getDungeonLoots(main, winLoots, failLoots);
		
		// Link du donjon au main (Attentio, ici il y'a une utilisation de surcharge de fonction)
		main.getActiveDongeons().add(this); // On ajoute à la liste des donjons actifs ce donjon
		main.getActiveDungeons().put(dungeonName, this); // On ajoute à la l'HashMap des donjons actifs le nom du donjon avec le donjoon
		this.activePlayers.forEach(player -> main.getLinkedDongeon().put(player.getUniqueId(), this)); // Pour chaque joueur actif, on ajoute son UUID ainsi que ce donjon, à la HashMap des joueurs dans des donjons
		
		// Boucle de vérification des vagues, sans fin sauf appel de cancel() , toutes les secondes
		Bukkit.getScheduler().runTaskTimer(main, new BukkitRunnable() {
			@Override
			public void run() {
				if(!getArea().isAliveNonPlayerEntityInWholeArea()) {  // S'il n'y'a plus d'entité dans la zone du donjon alors :
					if(!isNextWave()) { // S'il n'y pas d'autre vague alors
						stop_winning(); // Les joueurs gagnent : On leur donne leurs loots de victoire
						cancel(); // On stoppe la boucle
					} else if(getActivePlayers().isEmpty()) { // Sinon si la liste des joueurs en vie/actifs sur ce donjon est vide, alors :
						stop_loosing(); // Les joueurs perdent : On leur donne leurs loots de défaite
						cancel(); // On stoppe la boucle 
					} else { // Sinon
						// On récupère une Collection de toutes les entités environnantes à la localisation à analyser dans un rayon défini par le panneau de la prochaine vague
						Collection<? extends Entity> nearEntities = currentLocation.getWorld().getNearbyEntities(getCurrentSignLocation(), getCurrentRadius().getRadiusX(), getCurrentRadius().getRadiusY(), getCurrentRadius().getRadiusZ(), entity -> (entity instanceof Player));
						if(isCollectionContainingPlayers(nearEntities)) { // Si cette Collection contient des joueurs alors
							getCurrentWave().getValue().start(); // On démarre la vague suivante
							activeNextWave(); // On avance l'itérateur d'une vague
						}
					}
				}
			}
			
		}, 0l, (long)1 * 20);
	}
	
	/**
	 * Fonction exécutée à chaque mort d'un joueur
	 * @param event -> Event géré par Spigot
	 */
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity(); // On récupère le joueur
		if(this.activePlayers.contains(player)) { // Si le joueur fait partie des joueurs actifs dans ce donjon
			this.activePlayers.remove(player); // On retire le joueur de la liste des joueurs actifs dans ce donjon
			
			Location bedloc = player.getBedSpawnLocation(); // On récupère la localisation de respawn du joueur
			
			player.setBedSpawnLocation(dongeonActivationLocation); // On change son emplacement de respawn à l'endroit de départ du serveur
			player.spigot().respawn(); // On force le respawn du joueur
			player.setBedSpawnLocation(bedloc); // On re-change l'endroit de respawn du joueur à l'emplacement de respawn original
			
			player.teleport(this.dongeonActivationLocation.add(0, 0.1, 0)); // On téléporte le joueur à l'endroit de départ du serveur ( avec y + 0.1 en cas de glitch)
			player.sendMessage(Main.PLUGIN_PREFIX + "Vous avez malheureusement échoué dans ce donjon."); // On signale au joueur qu'il a échoué
		}
	}
	
	/**
	 * Fonction exécutée à chaque fermeture d'inventaire
	 * @param event -> Event géré par Spigot
	 */
	@EventHandler
	public void onInvClose(InventoryCloseEvent event) { 
		Inventory inv = event.getInventory(); // On récupère l'inventaire concerné par l'event
		if(event.getView().getTitle().equals(GameStatus.Win.getInvName()) || event.getView().getTitle().equals(GameStatus.Loose.getInvName())) { // Si le nom d'inventaire correspond à un des noms d'inventaire de loots de donjon alors
			if(!(Arrays.asList(inv.getContents()).isEmpty() && Arrays.asList(inv.getStorageContents()).isEmpty())) { // Si l'inventaire n'est pas rempli
				event.getPlayer().openInventory(inv); // On ré-ouvre l'inventaire au joueur
				event.getPlayer().sendMessage(Main.PLUGIN_PREFIX + "Vous devez récupérer tous le butin"); // On lui signale qu'il doit récupérer tous les loots
			}
		}
	
	}
	
	/**
	 * Fonction qui donne les loots de victoire aux joueurs encore connectés
	 */
	public void stop_winning() {
		List<Player> connectedPlayers = new ArrayList<Player>(); // On créé une liste de joueurs vide
		this.beginningPlayers.forEach(uuid -> { // Pour chaque UUID de joueur présent au départ du donjon
			try { // On essaie de récupérer le Player associé, puis de lui informer de la victoire
				Bukkit.getPlayer(uuid).sendMessage(Main.PLUGIN_PREFIX + "Votre équipe à réussi à compléter ce donjon. Bravo à tous !\n"
						+ "Voici votre butin pour vous récompensez, veuillez à partager en fonction des besoins de chacun !");
				Bukkit.getPlayer(uuid).teleport(this.dongeonActivationLocation); // On téléporte le joueur à l'entrée du donjon
				connectedPlayers.add(Bukkit.getPlayer(uuid)); // On ajoute ce joueur dans liste des joueurs encore connectés
				main.getLinkedDongeon().remove(uuid); // On retire le joueur de la HashMap des joueurs liés aux donjons
			} catch (NullPointerException e) { /*En cas de NullPointerException, ne rien faire*/ }
		});
		
		openLootsInv(connectedPlayers, loots, GameStatus.Win); // On donne les loots de victoire aux joueurs encore connectés 
		
		main.getActiveDongeons().remove(this); // On retire le donjon de la liste des donjons actifs
		main.getActiveDungeons().remove(this.dungeonName); // On retire le donjon de la HashMap des donjons actifs
	}
	
	/**
	 * Fonction qui donne les loots de défaite aux joueurs encore connectés
	 */
	public void stop_loosing() {
		List<Player> connectedPlayers = new ArrayList<Player>(); // On créé une liste de joueurs vide
		this.beginningPlayers.forEach(uuid -> { // Pour chaque UUID de joueur présent au départ du donjon
			try { // On essaie de récupérer le Player associé, puis de lui informer de la défaite
				Bukkit.getPlayer(uuid).sendMessage(Main.PLUGIN_PREFIX + "Votre équipe à échoué à ce donjon. Bonne chance pour la prochaine fois !"
						+ "Tenez, un dédommagement");
				Bukkit.getPlayer(uuid).teleport(this.dongeonActivationLocation); // On téléporte le joueur à l'entrée du donjon
				connectedPlayers.add(Bukkit.getPlayer(uuid)); // On ajoute ce joueur dans liste des joueurs encore connectés
				main.getLinkedDongeon().remove(uuid); // On retire le joueur de la HashMap des joueurs liés aux donjons
			} catch (NullPointerException e) { /*En cas de NullPointerException, ne rien faire*/ }
		});
		
		openLootsInv(connectedPlayers, loots, GameStatus.Loose); // On donne les loots de défaite aux joueurs encore connectés 
		
		main.getActiveDongeons().remove(this); // On retire le donjon de la liste des donjons actifs
		main.getActiveDungeons().remove(this.dungeonName); // On retire le donjon de la HashMap des donjons actifs
	}
	
	/**
	 * Permet de récupérer la zone du donjon
	 * @return DongeonArea -> Zone du donjon
	 */
	public DongeonArea getArea() {
		return this.area;
	}
	
	/**
	 * Permet de récupérer la localisation de départ du donjon
	 * @return Location -> Location de départ du donjon
	 */
	public Location getDungeonStartLocation() {
		return this.dongeonActivationLocation;
	}
	
	/**
	 * Permet de récupérer le rayon actuel qui est inspecté
	 * @return Radius -> Rayon actuel à inspecter
	 */
	private Radius getCurrentRadius() {
		return this.currentRadius;
	}
	
	/**
	 * Permet de récupérér la localisation actuelle à analyser 
	 * @return Location -> Location à analyser
	 */
	private Location getCurrentSignLocation() {
		return this.currentLocation;
	}
	
	/**
	 * Permet de savoir si l'itérateur des vagues possède une valeur suivante
	 * @return boolean -> reste-t'il une vague après celle en cours 
	 */
	public boolean isNextWave() {
		return this.currentIteration.hasNext();
	}
	
	/**
	 * Permet de savoir si la Collection contient des joueurs
	 * @param collection -> Collection d'entités/ mob qui est une classe enfant à la classe Entity à analyser
	 * @return boolean -> y'a t'il des joueurs dans cette collection
	 */
	public boolean isCollectionContainingPlayers(Collection<? extends Entity> collection) {
		for(Entity o : collection) { // Pour chaque entité de la collection
			if(!(o instanceof Player)) continue; // Si l'entité n'est pas un joueur, passer à l'entité suivante
			return true; // Sinon, renvoyer que la collection contient des joueurs (return true)
		}
		return false; // Renvoyer par défaut que la collection ne contient aucun joueur
	}
	
	/**
	 * Permet d'activer la vague suivante
	 */
	public void activeNextWave() {
		this.currentWave = this.currentIteration.next(); // On définit la vague courante, sur la vague suivante, tout en avançant d'une valeur l'itérateur
		this.currentRadius = Radius.getRadiusFromSignLocation(this.currentWave.getKey()); //  On définit la radius acutel avec un radius lu sur le panneau situé à l'emplacement indiqué par l'entrée de HashMap
		this.currentLocation = this.currentWave.getKey(); // On définit la localisation à analyser avec la l'Entry en cours
	}
	
	/**
	 * Permet de récupérer les joueurs qui sont actifs/en vie dans le donjon
	 * @return List<Player> -> Liste des joueurs actifs dans le donjon
	 */
	public List<Player> getActivePlayers(){
		return this.activePlayers;
	};
	
	/**
	 * Permet de récupérer la HashMap des vagues du donjon liées aux Locations à analyser
	 * @return HashMap<Location, Wave> -> HashMap des vagues liées aux Locations.
	 */
	public HashMap<Location, Wave> getWaveActivationLocations(){
		return this.waveActivationLocations;
	}

	/**
	 * Permet de récupérer l'entrée de la HashMap qui est en cours d'utilisation
	 * @return Entry<Location, Wave> -> Entrée de la HashMap en cours d'utilisation
	 */
	public Entry<Location, Wave> getCurrentWave() {
		return this.currentWave;
	}
	
	/**
	 * Fonction qui permet d'ouvrir les inventaires de loots aux joueurs
	 * @param players -> Joueurs à qui il faut ouvrir les inventaires
	 * @param loots -> Loots à donner
	 * @param status -> Statut de victoire/défaite des joueurs par rapport au donjon.
	 */
	public void openLootsInv(List<Player> players, DungeonLoots loots, GameStatus status) {
			   if(status == GameStatus.Win) { // Si les joueurs on gagné alors
			Inventory inv = Bukkit.createInventory(null, 6 * 9, status.getInvName()); // On créé un inventaire avec le nom des inventaires de victoire
			loots.getWinLoots().forEach(item -> { // pour chaque item des loots de victoire :
				inv.addItem(item); // On ajoute l'item dans l'inventaire
			});
			players.forEach(player -> player.openInventory(inv)); // pour chaque joueur de la liste, on lui fait ouvrir l'inventaire
		} else if(status == GameStatus.Loose) { // Si les joueurs on perdu alors
			Inventory inv = Bukkit.createInventory(null, 6 * 9, status.getInvName()); // On créé un inventaire avec le nom des inventaires de défaite
			loots.getLooseLoots().forEach(item -> { // pour chaque item des loots de défaite :
				inv.addItem(item); // On ajoute l'item dans l'inventaire
			});
			players.forEach(player -> player.openInventory(inv)); // pour chaque joueur de la liste, on lui fait ouvrir l'inventaire
		}
	}
	
	/**
	 * Permet de définir le statut des joueurs quand à la fin du donjon.
	 * @author Altaks
	 */
	public enum GameStatus {
		
		Win("§8"+"Voici vos gains, valeureux gerriers"), 
		Loose("§8"+"Vous ferez mieux la prochaine fois");
		
		private String inventoryName; // Nom de l'inventaire à ouvrir
		
		/**
		 * Constructeur qui permet de définir une valeur de l'énum GameStatus
		 * @param inventoryName -> Définit le nom de l'inventaire en fonction de la valeur choisie
		 */
		private GameStatus(String inventoryName) {
			this.inventoryName = inventoryName;
		}
		
		/**
		 * Permet de récupérer le nom de l'inventaire en fonction de la valeur choisie
		 * @return
		 */
		public String getInvName() {
			return this.inventoryName;
		}
		
	}
	
}

/**
 * Classe qui permet de faciliter l'analyse des entités dans un rayon donné
 * @author Altaks
 */
class Radius {
	
	// Rayons en X, Y et Z
	private double xrad, yrad, zrad;
	
	/**
	 * Constructeur du Rayon d'analyse
	 * @param xrad -> Radius en X
	 * @param yrad -> Radius en Y
	 * @param zrad -> Radius en Z
	 */
	public Radius(double xrad, double yrad, double zrad) {
		this.xrad = xrad;
		this.yrad = yrad;
		this.zrad = zrad;
	}
	
	/**
	 * Permet de lire un rayon sur un panneau
	 * @param location -> Emplacement du panneau à lire
	 * @return Radius -> Rayon d'analyse
	 */
	public static Radius getRadiusFromSignLocation(Location location) {
		Sign sign = (Sign)location.getBlock(); // On récupère le panneau à l'emplacement indiqué
		
		String[] lines = sign.getLines(); // On récupère les lignes du panneau
		
		String[] radiuses = lines[2].split("/"); // On récup les rayons X, Y et Z sur la 3e ligne du panneau en divisant la ligne au niveau des "/"
		
		// On convertit les String en double
		double xrad = Double.parseDouble(radiuses[0]);
		double yrad = Double.parseDouble(radiuses[1]);
		double zrad = Double.parseDouble(radiuses[2]);
		
		return new Radius(xrad, yrad, zrad); // On renvoie le Rayon lu
	}
	
	/**
	 * Permet de récupérer le Rayon à analyser en X
	 * @return Double -> Rayon en X à analyser
	 */
	public double getRadiusX() {
		return this.xrad;
	}
	
	/**
	 * Permet de récupérer le Rayon à analyser en Y
	 * @return Double -> Rayon en Y à analyser
	 */
	public double getRadiusY() {
		return this.yrad;
	}
	
	/**
	 * Permet de récupérer le Rayon à analyser en Z
	 * @return Double -> Rayon en Z à analyser
	 */
	public double getRadiusZ() {
		return this.zrad;
	}
}
