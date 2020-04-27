package fr.altaks.eodungeons.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.util.LootsUtil;

/**
 * @author Altaks
 */
public class Dongeon implements Listener {
	
	private final List<Player> activePlayers = new ArrayList<Player>(); // Liste des joueurs encore actifs dans le donjon
	private final List<UUID> beginningPlayers = new ArrayList<UUID>(); // Liste des joueurs actifs au départ du donjon
	
	private final LinkedHashMap<Location, Wave> waveActivationLocations = new LinkedHashMap<Location, Wave>(); // HashMap des Locations de panneaux -> vagues
	
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
		this.dongeonActivationLocation = dongeonActivationLocation;
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Donjon " + dungeonName + " chargé"));
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
		this.dongeonActivationLocation = dongeonActivationLocation;
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Donjon " + dungeonName + " chargé"));
	}
	
	/**
	 * Fonction pour lancer un donjon
	 */
	public void start() {
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Donjon " + dungeonName + " lancé"));
		// Link du donjon au main (Attention, ici il y'a une utilisation de surcharge de fonction)
		if(main.getActiveDongeons().contains(this) || main.getActiveDungeons().containsValue(this)) {
			if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Ce donjon est déjà lancé"));
			return; // Si le donjon est déja actif, arrêter de lire la fonction.
		}
		
		main.getActiveDongeons().add(this); // On ajoute à la liste des donjons actifs ce donjon
		main.getActiveDungeons().put(dungeonName, this); // On ajoute à la l'HashMap des donjons actifs le nom du donjon avec le donjoon
		this.activePlayers.forEach(player -> main.getLinkedDongeon().put(player.getUniqueId(), this)); // Pour chaque joueur actif, on ajoute son UUID ainsi que ce donjon, à la HashMap des joueurs dans des donjons
		
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Chargement des listes de mobs..."));
		Sign[] systemSigns = area.getSystemSignsFromArea(); // On récupère les panneaux de la zone du donjon, sous forme de tableau déja trié
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§e\u00BB Zone du donjon : \n"
				+ "POS_1 > x:" + area.getMinLoc().getBlockX() + "/y:" + area.getMinLoc().getBlockY() + "/z:" + area.getMinLoc().getBlockZ() + "\n"
				+ "POS_2 > x:" + area.getMaxLoc().getBlockX() + "/y:" + area.getMaxLoc().getBlockY() + "/z:" + area.getMaxLoc().getBlockZ()));
		for(Sign sign : systemSigns) { // pour chaque panneau du tableau
			try {
				File file = new File(main.getWaveDirectory() + File.separator + sign.getLines()[1].split(":")[1].toString() + ".yml");
				if(!file.exists() && Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§e\u00BB Fichier de la liste " + file.getName() + " inexistante"));
				// on ajoute à la HashMap l'emplacement du panneau avec la vague (importée depuis un fichier)
				waveActivationLocations.put(sign.getLocation(), Wave.loadWaveFromYmlFile(this, main, file));
				if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Liste " + sign.getLine(0).split(":")[1] + " chargée"));
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
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Listes chargées"));
		
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Chargement des loots..."));
		// On récupère les fichiers des listes de loots
		File winLoots = new File(main.getLootsDirectory() + File.separator + dungeonName + "_win.yml");
		File failLoots = new File(main.getLootsDirectory() + File.separator + dungeonName + "_loose.yml");
		
		// On affecte les Loots du donjon avec les loots lus sur les deux fichiers
		this.loots = LootsUtil.getDungeonLoots(main, winLoots, failLoots);
		
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Loots chargés"));
				
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Lancement de la boucle des vagues"));
		// Boucle de vérification des vagues, sans fin sauf appel de cancel() , toutes les secondes
		
		new BukkitRunnable() {
			
			final Iterator<Entry<Location, Wave>> waveIterator = getWaveActivationLocations().entrySet().iterator();
			Entry<Location, Wave> currentWave = waveIterator.next();
			
			Radius currentRadius = Radius.getRadiusFromSignLocation(currentWave.getKey());
			{
				if(Main.isDebugging) getActivePlayers().forEach(p -> {
					p.sendMessage("§c\u00BB 1ère vague chargée, rayon §6" + currentRadius.getRadiusX() + "/" + currentRadius.getRadiusY() + "/" + currentRadius.getRadiusZ() + "\n"
							+ "§cà partir de x:" + currentWave.getKey().getBlockX() + "/y:" + currentWave.getKey().getBlockY() + "/z:" + currentWave.getKey().getBlockX()) ;
					p.sendMessage("§6\u00BB Nombre de vagues : " + getWaveActivationLocations().entrySet().size());
				});
			}
			
			@Override
			public void run() {
				if(!getArea().isAliveNonPlayerEntityInWholeArea()) {  // S'il n'y'a plus d'entité dans la zone du donjon alors :
					
					if(Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Il n'y a pas de mob non joueur dans la zone"));
					
					if(getActivePlayers().isEmpty()) {
						stop_loosing(false);
						cancel();
					} else if((!waveIterator.hasNext()) && (!getArea().isAliveNonPlayerEntityInWholeArea()) && currentWave.getValue().isLaunched()){
						if(Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Les joueurs ont gagné !"));
						stop_winning();
						cancel();
					} else {
						Collection<Entity> collection = currentWave.getKey().getWorld().getNearbyEntities(currentWave.getKey(), currentRadius.getRadiusX(), currentRadius.getRadiusY(), currentRadius.getRadiusZ(), e -> (e instanceof Player));
						
						if(isCollectionContainingPlayers(collection)) {
							// Changer la vague & faire spawn les mobs
							currentWave.getValue().start();
							currentWave = waveIterator.next();
							
							if(Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Passage à la prochaine vague"));
						
						} else {
							// Logger la non présence des joueurs dans la zone d'analyse
							if(Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Il n'y a aucun joueur dans la zone d'analyse ..."));
						}
					}
					
				} else if(getActivePlayers().isEmpty()){
					stop_loosing(false);
					cancel();
				} else {
					if(Main.isDebugging) getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Il reste des mobs dans la zone"));
				}
			}
			
		}.runTaskTimer(main, 0l, (long) 1 * 20);
		
		if(Main.isDebugging) this.activePlayers.forEach(p -> p.sendMessage("§c\u00BB Boucle des listes lancée"));
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
//	@EventHandler
//	public void onInvClose(InventoryCloseEvent event) { 
//		Inventory inv = event.getInventory(); // On récupère l'inventaire concerné par l'event
//		if(event.getView().getTitle().equals(GameStatus.Win.getInvName()) || event.getView().getTitle().equals(GameStatus.Loose.getInvName())) { // Si le nom d'inventaire correspond à un des noms d'inventaire de loots de donjon alors
//			if(inv.getStorageContents().length != 0) {
//				event.getPlayer().openInventory(inv); // On ré-ouvre l'inventaire au joueur
//				event.getPlayer().sendMessage(Main.PLUGIN_PREFIX + "Vous devez récupérer tous le butin"); // On lui signale qu'il doit récupérer tous les loots
//			}
//		}
//	
//	}
	
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
	public void stop_loosing(boolean forced) {
		List<Player> connectedPlayers = new ArrayList<Player>(); // On créé une liste de joueurs vide
		this.beginningPlayers.forEach(uuid -> { // Pour chaque UUID de joueur présent au départ du donjon
			try { // On essaie de récupérer le Player associé, puis de lui informer de la défaite
				Player player = Bukkit.getPlayer(uuid);
				player.sendMessage(Main.PLUGIN_PREFIX + "Votre équipe à échoué à ce donjon. Bonne chance pour la prochaine fois !"
						+ "Tenez, un dédommagement");
				player.teleport(this.dongeonActivationLocation); // On téléporte le joueur à l'entrée du donjon
				connectedPlayers.add(Bukkit.getPlayer(uuid)); // On ajoute ce joueur dans liste des joueurs encore connectés
				main.getLinkedDongeon().remove(uuid); // On retire le joueur de la HashMap des joueurs liés aux donjons
			} catch (NullPointerException e) { /*En cas de NullPointerException, ne rien faire*/ }
		});
		
		if(!forced) openLootsInv(connectedPlayers, loots, GameStatus.Loose); // On donne les loots de défaite aux joueurs encore connectés 
		
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
	 * Permet de savoir si la Collection contient des joueurs
	 * @param collection -> Collection d'entités/ mob qui est une classe enfant à la classe Entity à analyser
	 * @return boolean -> y'a t'il des joueurs dans cette collection
	 */
	public boolean isCollectionContainingPlayers(Collection<? extends Entity> collection) {
		for(Entity o : collection) { // Pour chaque entité de la collection
			if(!(o instanceof Player)) {
				continue; // Si l'entité n'est pas un joueur, passer à l'entité suivante
			} else if(this.activePlayers.contains((Player)o)) return true; // Sinon, si le joueur est un joueur faisant partie des joueurs normalement présents dans ce donjon, renvoyer que la collection contient des joueurs (return true)
		}
		return false; // Renvoyer par défaut que la collection ne contient aucun joueur
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
	public LinkedHashMap<Location, Wave> getWaveActivationLocations(){
		return this.waveActivationLocations;
	}

	
	
	/**
	 * Fonction qui permet d'ouvrir les inventaires de loots aux joueurs
	 * @param players -> Joueurs à qui il faut ouvrir les inventaires
	 * @param loots -> Loots à donner
	 * @param status -> Statut de victoire/défaite des joueurs par rapport au donjon.
	 */
	public void openLootsInv(List<Player> players, DungeonLoots loots, GameStatus status) {	   
		Inventory inv = Bukkit.createInventory(null, 6 * 9, status.getInvName()); // On créé un inventaire avec le nom des inventaires de victoire		
		if(status.equals(GameStatus.Win)) {
			for(ItemStack item : loots.getWinLoots()) {
				inv.addItem(item);
			}
		} else if(status.equals(GameStatus.Loose)) {
			for(ItemStack item : loots.getWinLoots()) {
				inv.addItem(item);
			}
		} else return;
		players.forEach(player -> player.openInventory(inv)); // pour chaque joueur de la liste, on lui fait ouvrir l'inventaire
		return;
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
		Sign sign = (Sign) location.getBlock().getState(); // On récupère le panneau à l'emplacement indiqué
		
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
