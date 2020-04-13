package fr.altaks.eodungeons.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.core.Dongeon;
import fr.altaks.eodungeons.core.DongeonArea;

/**
 * @author Altaks
 */
public class InteractListener implements Listener {
	
	private List<Material> interactableSignTypes = new ArrayList<Material>(); // Liste des types de panneaux avec lesquels les joueurs pourront interagir
	private List<Material> systemSignTypes = new ArrayList<Material>(); // Liste des types de panneaux avec lesquels le plugin va interagir
	{
		Material[] interactionSignTypes = { Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN }; 
		this.interactableSignTypes.addAll(Arrays.asList(interactionSignTypes)); // On rajoute les panneaux en chêne noir à la liste des panneaux avec lesquels les joueurs pourront interagir

		Material[] systemSignTypes = { Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN };
		this.systemSignTypes.addAll(Arrays.asList(systemSignTypes)); // On rajoute les panneaux en acacia à la liste des panneaux avec lesquels le plugin va interagir
	}
	
	private Main main; // Variable qui va stocker la classe Main
	
	public InteractListener(Main main) { // Constructeur qui permettra l'affectation de la variable main ci-dessus
		this.main = main;
	}
	
	/**
	 * Fonction executée à chaque interaction d'un joueur (à chaque click hors inventaires)
	 * @param event -> paramètre géré par Spigot
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent event) { 
		if(!event.hasBlock() || event.getClickedBlock().getType().equals(Material.AIR) || event.getClickedBlock().getType().equals(Material.CAVE_AIR)) return; // Si il n'y pas de block cliqué ou qu'il s'agit d'air alors arrêter de lire la fonction
		Player player = event.getPlayer(); // On stocke le joueur dans une variable
		Block block = event.getClickedBlock(); // On stocke le block sur lequel le joueur cliqué dans une variable
		
		if(this.interactableSignTypes.contains(block.getType()) && block.getWorld().getName().equalsIgnoreCase("World_DJ_EO")) { // si le type de block cliqué est dans la liste des panneaux cliquables et que le block est dans le monde "World_DJ_EO" alors
			Sign sign = getLinkedSign((Sign)block); // On récupère le panneau à l'opposé du panneau cliqué
			if(getDongeonSignTypes(sign).equals(DongeonSignTypes.STARTER)) { // Si le panneau est un panneau de lancement de donjon alors
				
				// Pattern des panneaux de démarrage :
				
				// LINE 0 : STATE.START
				// LINE 1 : d:<dungeon-name>
				// LINE 2 : <min-x>/<min-y>/<min-z>
				// LINE 3 : <max-x>/<max-y>/<max-z>
				
				Location minLoc = readFromStringLocations(sign, sign.getLine(2)); // On récupère la position minimale extrème du donjon
				Location maxLoc = readFromStringLocations(sign, sign.getLine(3)); // On récupère la position maximale extrème du donjon 
				
				launchDungeon((Sign) block, player, sign.getLine(1).split(":")[1], minLoc, maxLoc); // On lance le donjon
			}
		}
		
	}
	
	/**
	 * Permet de récupérer une Location depuis les infos d'une ligne de panneau
	 * @param sign -> panneau inspecté
	 * @param str -> ligne inspectée
	 * @return Location -> Location inscrite sur le panneau dans le même monde
	 */
	public Location readFromStringLocations(Sign sign, String str) {
		
		String strX = str.split("/")[0]; // On récupère les coords en X
		String strY = str.split("/")[1]; // On récupère les coords en Y
		String strZ = str.split("/")[2]; // On récupère les coords en Z
		
		double x = Double.parseDouble(strX), y = Double.parseDouble(strY), z = Double.parseDouble(strZ); // On convertit les coords String en coords Double.
		return new Location(sign.getWorld(), x, y, z); // On renvoie la Location écrite
	}
	
	/**
	 * Permet d'obtenir le panneau à l'opposé du panneau cliqué
	 * @param sign -> panneau cliqué
	 * @return Sign -> panneau à l'opposé du panneau cliqué
	 * @throws NullPointerException -> peut renvoyer un bloc autre qu'un panneau et provoquer une NullPointerException
	 */
	public Sign getLinkedSign(Sign sign) throws NullPointerException {
		Directional directional = (Directional) sign; // On récupère le panneau en tant que bloc directionnel
		BlockFace facing = directional.getFacing(); // On récupère la direction un panneau

		if (this.interactableSignTypes.contains(sign.getType())) { // si le panneau fait partie des panneau avec lesquels les joueurs peuvent intéragir alors
			if (sign.getType().equals(Material.DARK_OAK_WALL_SIGN)) { // Si le panneau est un panneau mural alors
				Location nextLocation = sign.getLocation(); // On récupère le placement du panneau actuel
				switch (facing) { // en fonction de la direction du panneau clické on décale la "nextLocation" à 2 blocs opposé.
				case NORTH:
					nextLocation.add(0, 0, -2);
					break;
				case SOUTH:
					nextLocation.add(0, 0, 2);
					break;
				case EAST:
					nextLocation.add(-2, 0, 0);
					break;
				case WEST:
					nextLocation.add(2, 0, 0);
					break;
				default:
					return null;
				}
				if (this.interactableSignTypes.contains(nextLocation.getBlock().getType())) { // si à la nouvelle Location il y'a bien un panneau avec lequel les joueurs peuvent intéragir alors
					Sign newSign = (Sign) nextLocation.getBlock(); // on récup le block en tant que Sign
					return newSign; // on renvoie le sign
				}
			} else if (sign.getType().equals(Material.DARK_OAK_SIGN)) { // Si il s'agit d'un panneau simple (non mural) (décalage 3 blocks en dessous)
				Location nextLocation = sign.getLocation(); // On récupère la position du bloc
				nextLocation.add(0, -3, 0); // on retire 3 au Y
				if (this.interactableSignTypes.contains(nextLocation.getBlock().getType())) { // si à la nouvelle Location il y'a bien un panneau avec lequel les joueurs peuvent intéragir alors
					Sign newSign = (Sign) nextLocation.getBlock(); // on récup le block en tant que Sign
					return newSign; // on revoie le blocks
				}
			} else
				return null; // Sinon on revoie rien
		} else
			return null; // Sinon on revoie rien

		return null; // Si aucune condition n'est remplie, on revoie rien
	}
	
	/**
	 * Permet de vérifier le type de panneau.
	 * @param sign -> panneau a vérifier
	 * @return DungeonSignTypes -> Type de panneau de donjon
	 * @throws NullPointerException peut provoquer une erreur si aucune des condition n'est remplie
	 */
	public DongeonSignTypes getDongeonSignTypes(Sign sign) throws NullPointerException {
		String[] lines = sign.getLines(); // On récupère les lignes du panneau en tant que tableau de String
		if(lines[0].equalsIgnoreCase(DongeonSignTypes.STARTER.systemKey())) { // s'il s'agit d'un panneau de lancement alors
			return DongeonSignTypes.STARTER; // renvoyer le type STARTER de l'énumération
		} else if(lines[1].equalsIgnoreCase(DongeonSignTypes.STOPPER.systemKey())) { // s'il s'agit d'un panneau d'arrêt alors (note du dev : non-utilisé pour l'instant)
			return DongeonSignTypes.STOPPER; // renvoyer le type STOPPER de l'énumération
		}
		return null; // Si aucune des conditions n'est remplie, on renvoie rien.
	}
	
	/**
	 * Permet de vérifier les conditions et lance le donjon
	 * @param sign -> panneau sur lequel le joueur qui a lancé le donjon a cliqué 
	 * @param player -> joueur ayant cliqué pour lancer le donjon
	 * @param dungeonName -> nom du donjon lancé
	 * @param minLoc -> coordonnées du coin minimal du donjon
	 * @param maxLoc -> coordonnées du coin maximal du donjon
	 */
	public void launchDungeon(Sign sign, Player player, String dungeonName, Location minLoc, Location maxLoc) {
		
		if(main.getActiveDungeons().containsKey(dungeonName)) { // Si le donjon est déjà lancé alors
			player.sendMessage(Main.PLUGIN_PREFIX + "Ce donjon est actuellement occupé"); // envoyer un message de refus au joueur qui souhaite lancer le donjon
			return; // Arrêter la lecture de la fonction ici
		}
		
		Collection<Player> players = getAllNearPlayers(player); // On récupère tous les joueurs à proximité des coords du joueur ayant cliqué dans un rayon de 5.5 blocs
		for(Player p : players) { // pour chaque joueur dans "players"
			if(p.getInventory().contains(Material.ELYTRA)) { // Si le joueur possède des élytres alors 
				players.forEach(py -> { // pour chaque joueur dans "players"
					// On envoie un message de refus pour possession d'élytres
					py.sendMessage(Main.PLUGIN_PREFIX + "Un des membres de votre équipe possède des élytres. Il s'agit d'un objet interdit dans les donjons. Ce donjon ne se lancera pas si ce joueur les garde");
					return; // On stoppe la lecture de la fonction
				});
			} else continue; // Si le joueur ne possède pas d'élytres, passer au joueur suivant
		}
		
		if(players.size() > 4) { // Si le groupe comporte plus de 4 joueurs alors
			// Pour chaque joueur : envoyer un message de refus pour surplus de joueurs dans l'équipe
			players.forEach(py -> py.sendMessage(Main.PLUGIN_PREFIX + "Votre équipe contient plus de 4 joueurs, le donjon ne peut pas se lancer"));
			return; // Arrêter la lecture de la fonction ici
		}
		
		// Téléporter les joueurs dans le donjon, à 10 blocks devant eux avec la direction dans laquelle ils regardaient
		teleportPlayersFromClickedSign(sign, player, players);
		
		DongeonArea area = new DongeonArea(minLoc, maxLoc); // On récupère la zone du donjon
		Dongeon newInstance = new Dongeon(main, player.getLocation(), dungeonName, area, players); // On génère une nouvelle instance du donjon
		main.getServer().getPluginManager().registerEvents(newInstance, main); // On enregistre cette instance comme Listener indépendant
		newInstance.start(); // On lance le donjon en interne
	}
	
	/**
	 * Fonction permettant de téléporter les joueurs dans le donjon à 10 blocks devant eux, avec une direction opposée à celle du panneau cliqué
	 * @param sign -> panneau cliqué par le joueur
	 * @param clicker -> Joueur ayant cliqué
	 * @param players -> Groupe de joueurs voulant essayer le donjon
	 */
	public void teleportPlayersFromClickedSign(Sign sign, Player clicker, Collection<Player> players) {
		Directional directional = (Directional) sign; // On récupère le panneau en tant que block directionnel
		BlockFace facing = directional.getFacing(); // On récupère l'orientation du panneau en question
		
		if(this.interactableSignTypes.contains(sign.getType())) { // Si le panneau est bien un panneau avec lequel un joueur peut intéragir alors
			Location nextLocation = clicker.getLocation(); // On récupère la location du joueur ayant cliqué.
			switch (facing) { // En fonction de l'orientation du panneau : on ajoute 10 blocs dans une direction donnée 
			
			/*
			 * Nord -> +X
			 * Sud -> -X
			 * 
			 * Est -> +Z
			 * Ouest -> -Z
			 */
			
			case NORTH:
				nextLocation.add(0, 0, -10);
				break;
			case SOUTH:
				nextLocation.add(0, 0, 10);
				break;
			case EAST:
				nextLocation.add(-10, 0, 0);
				break;
			case WEST:
				nextLocation.add(10, 0, 0);
				break;
			default:
				break;
			}
			
			players.forEach(p -> { // pour chaque joueur du groupe
				p.teleport(nextLocation); // téléporter le joueur au nouvel emplacement
			});
		}
	}
	
	/**
	 * Permet d'obtenir tous les joueurs dans un rayon de 5,5 blocks (reach d'un joueur lambda)
	 * @param player -> Joueur qui a cliqué
	 * @return Collection de joueurs qui contient tous les joueurs autour du joueur qui à cliqué dans un rayon de 5,5 block + le joueur ayant cliqué
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Player> getAllNearPlayers(Player player) {
		return (Collection) player.getWorld().getNearbyEntities(player.getLocation(), 5.5d, 5.5d, 5.5d, e -> (e instanceof Player));
	}

}

enum DongeonSignTypes {
	
	STARTER("STATE.START"), // Panneau de lancement
	STOPPER("STATE.STOP"); // Panneau d'arrêt (Note du dev : non-utilisé pour l'instant)
	
	private String systemKey; // Variable de stockage de la clé système
	
	/**
	 * Constructeur pour stocker la clé système
	 * @param systemKey -> clé système
	 */
	private DongeonSignTypes(String systemKey) {
		this.systemKey = systemKey;
	}
	
	/**
	 * Permet de récupérer la clé système qui sera écrite sur le panneau en première ligne (soit lines[0])
	 * @return String -> Clé système
	 */
	public String systemKey() {
		return this.systemKey;
	}
 	
}

