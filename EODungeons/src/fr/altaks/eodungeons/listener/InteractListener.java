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
		this.interactableSignTypes.addAll(Arrays.asList(interactionSignTypes)); // On rajoute les panneaux en ch�ne noir � la liste des panneaux avec lesquels les joueurs pourront interagir

		Material[] systemSignTypes = { Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN };
		this.systemSignTypes.addAll(Arrays.asList(systemSignTypes)); // On rajoute les panneaux en acacia � la liste des panneaux avec lesquels le plugin va interagir
	}
	
	private Main main; // Variable qui va stocker la classe Main
	
	public InteractListener(Main main) { // Constructeur qui permettra l'affectation de la variable main ci-dessus
		this.main = main;
	}
	
	/**
	 * Fonction execut�e � chaque interaction d'un joueur (� chaque click hors inventaires)
	 * @param event -> param�tre g�r� par Spigot
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent event) { 
		if(!event.hasBlock() || event.getClickedBlock().getType().equals(Material.AIR) || event.getClickedBlock().getType().equals(Material.CAVE_AIR)) return; // Si il n'y pas de block cliqu� ou qu'il s'agit d'air alors arr�ter de lire la fonction
		Player player = event.getPlayer(); // On stocke le joueur dans une variable
		Block block = event.getClickedBlock(); // On stocke le block sur lequel le joueur cliqu� dans une variable
		
		if(this.interactableSignTypes.contains(block.getType()) && block.getWorld().getName().equalsIgnoreCase("World_DJ_EO")) { // si le type de block cliqu� est dans la liste des panneaux cliquables et que le block est dans le monde "World_DJ_EO" alors
			Sign sign = getLinkedSign((Sign)block); // On r�cup�re le panneau � l'oppos� du panneau cliqu�
			if(getDongeonSignTypes(sign).equals(DongeonSignTypes.STARTER)) { // Si le panneau est un panneau de lancement de donjon alors
				
				// Pattern des panneaux de d�marrage :
				
				// LINE 0 : STATE.START
				// LINE 1 : d:<dungeon-name>
				// LINE 2 : <min-x>/<min-y>/<min-z>
				// LINE 3 : <max-x>/<max-y>/<max-z>
				
				Location minLoc = readFromStringLocations(sign, sign.getLine(2)); // On r�cup�re la position minimale extr�me du donjon
				Location maxLoc = readFromStringLocations(sign, sign.getLine(3)); // On r�cup�re la position maximale extr�me du donjon 
				
				launchDungeon((Sign) block, player, sign.getLine(1).split(":")[1], minLoc, maxLoc); // On lance le donjon
			}
		}
		
	}
	
	/**
	 * Permet de r�cup�rer une Location depuis les infos d'une ligne de panneau
	 * @param sign -> panneau inspect�
	 * @param str -> ligne inspect�e
	 * @return Location -> Location inscrite sur le panneau dans le m�me monde
	 */
	public Location readFromStringLocations(Sign sign, String str) {
		
		String strX = str.split("/")[0]; // On r�cup�re les coords en X
		String strY = str.split("/")[1]; // On r�cup�re les coords en Y
		String strZ = str.split("/")[2]; // On r�cup�re les coords en Z
		
		double x = Double.parseDouble(strX), y = Double.parseDouble(strY), z = Double.parseDouble(strZ); // On convertit les coords String en coords Double.
		return new Location(sign.getWorld(), x, y, z); // On renvoie la Location �crite
	}
	
	/**
	 * Permet d'obtenir le panneau � l'oppos� du panneau cliqu�
	 * @param sign -> panneau cliqu�
	 * @return Sign -> panneau � l'oppos� du panneau cliqu�
	 * @throws NullPointerException -> peut renvoyer un bloc autre qu'un panneau et provoquer une NullPointerException
	 */
	public Sign getLinkedSign(Sign sign) throws NullPointerException {
		Directional directional = (Directional) sign; // On r�cup�re le panneau en tant que bloc directionnel
		BlockFace facing = directional.getFacing(); // On r�cup�re la direction un panneau

		if (this.interactableSignTypes.contains(sign.getType())) { // si le panneau fait partie des panneau avec lesquels les joueurs peuvent int�ragir alors
			if (sign.getType().equals(Material.DARK_OAK_WALL_SIGN)) { // Si le panneau est un panneau mural alors
				Location nextLocation = sign.getLocation(); // On r�cup�re le placement du panneau actuel
				switch (facing) { // en fonction de la direction du panneau click� on d�cale la "nextLocation" � 2 blocs oppos�.
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
				if (this.interactableSignTypes.contains(nextLocation.getBlock().getType())) { // si � la nouvelle Location il y'a bien un panneau avec lequel les joueurs peuvent int�ragir alors
					Sign newSign = (Sign) nextLocation.getBlock(); // on r�cup le block en tant que Sign
					return newSign; // on renvoie le sign
				}
			} else if (sign.getType().equals(Material.DARK_OAK_SIGN)) { // Si il s'agit d'un panneau simple (non mural) (d�calage 3 blocks en dessous)
				Location nextLocation = sign.getLocation(); // On r�cup�re la position du bloc
				nextLocation.add(0, -3, 0); // on retire 3 au Y
				if (this.interactableSignTypes.contains(nextLocation.getBlock().getType())) { // si � la nouvelle Location il y'a bien un panneau avec lequel les joueurs peuvent int�ragir alors
					Sign newSign = (Sign) nextLocation.getBlock(); // on r�cup le block en tant que Sign
					return newSign; // on revoie le blocks
				}
			} else
				return null; // Sinon on revoie rien
		} else
			return null; // Sinon on revoie rien

		return null; // Si aucune condition n'est remplie, on revoie rien
	}
	
	/**
	 * Permet de v�rifier le type de panneau.
	 * @param sign -> panneau a v�rifier
	 * @return DungeonSignTypes -> Type de panneau de donjon
	 * @throws NullPointerException peut provoquer une erreur si aucune des condition n'est remplie
	 */
	public DongeonSignTypes getDongeonSignTypes(Sign sign) throws NullPointerException {
		String[] lines = sign.getLines(); // On r�cup�re les lignes du panneau en tant que tableau de String
		if(lines[0].equalsIgnoreCase(DongeonSignTypes.STARTER.systemKey())) { // s'il s'agit d'un panneau de lancement alors
			return DongeonSignTypes.STARTER; // renvoyer le type STARTER de l'�num�ration
		} else if(lines[1].equalsIgnoreCase(DongeonSignTypes.STOPPER.systemKey())) { // s'il s'agit d'un panneau d'arr�t alors (note du dev : non-utilis� pour l'instant)
			return DongeonSignTypes.STOPPER; // renvoyer le type STOPPER de l'�num�ration
		}
		return null; // Si aucune des conditions n'est remplie, on renvoie rien.
	}
	
	/**
	 * Permet de v�rifier les conditions et lance le donjon
	 * @param sign -> panneau sur lequel le joueur qui a lanc� le donjon a cliqu� 
	 * @param player -> joueur ayant cliqu� pour lancer le donjon
	 * @param dungeonName -> nom du donjon lanc�
	 * @param minLoc -> coordonn�es du coin minimal du donjon
	 * @param maxLoc -> coordonn�es du coin maximal du donjon
	 */
	public void launchDungeon(Sign sign, Player player, String dungeonName, Location minLoc, Location maxLoc) {
		
		if(main.getActiveDungeons().containsKey(dungeonName)) { // Si le donjon est d�j� lanc� alors
			player.sendMessage(Main.PLUGIN_PREFIX + "Ce donjon est actuellement occup�"); // envoyer un message de refus au joueur qui souhaite lancer le donjon
			return; // Arr�ter la lecture de la fonction ici
		}
		
		Collection<Player> players = getAllNearPlayers(player); // On r�cup�re tous les joueurs � proximit� des coords du joueur ayant cliqu� dans un rayon de 5.5 blocs
		for(Player p : players) { // pour chaque joueur dans "players"
			if(p.getInventory().contains(Material.ELYTRA)) { // Si le joueur poss�de des �lytres alors 
				players.forEach(py -> { // pour chaque joueur dans "players"
					// On envoie un message de refus pour possession d'�lytres
					py.sendMessage(Main.PLUGIN_PREFIX + "Un des membres de votre �quipe poss�de des �lytres. Il s'agit d'un objet interdit dans les donjons. Ce donjon ne se lancera pas si ce joueur les garde");
					return; // On stoppe la lecture de la fonction
				});
			} else continue; // Si le joueur ne poss�de pas d'�lytres, passer au joueur suivant
		}
		
		if(players.size() > 4) { // Si le groupe comporte plus de 4 joueurs alors
			// Pour chaque joueur : envoyer un message de refus pour surplus de joueurs dans l'�quipe
			players.forEach(py -> py.sendMessage(Main.PLUGIN_PREFIX + "Votre �quipe contient plus de 4 joueurs, le donjon ne peut pas se lancer"));
			return; // Arr�ter la lecture de la fonction ici
		}
		
		// T�l�porter les joueurs dans le donjon, � 10 blocks devant eux avec la direction dans laquelle ils regardaient
		teleportPlayersFromClickedSign(sign, player, players);
		
		DongeonArea area = new DongeonArea(minLoc, maxLoc); // On r�cup�re la zone du donjon
		Dongeon newInstance = new Dongeon(main, player.getLocation(), dungeonName, area, players); // On g�n�re une nouvelle instance du donjon
		main.getServer().getPluginManager().registerEvents(newInstance, main); // On enregistre cette instance comme Listener ind�pendant
		newInstance.start(); // On lance le donjon en interne
	}
	
	/**
	 * Fonction permettant de t�l�porter les joueurs dans le donjon � 10 blocks devant eux, avec une direction oppos�e � celle du panneau cliqu�
	 * @param sign -> panneau cliqu� par le joueur
	 * @param clicker -> Joueur ayant cliqu�
	 * @param players -> Groupe de joueurs voulant essayer le donjon
	 */
	public void teleportPlayersFromClickedSign(Sign sign, Player clicker, Collection<Player> players) {
		Directional directional = (Directional) sign; // On r�cup�re le panneau en tant que block directionnel
		BlockFace facing = directional.getFacing(); // On r�cup�re l'orientation du panneau en question
		
		if(this.interactableSignTypes.contains(sign.getType())) { // Si le panneau est bien un panneau avec lequel un joueur peut int�ragir alors
			Location nextLocation = clicker.getLocation(); // On r�cup�re la location du joueur ayant cliqu�.
			switch (facing) { // En fonction de l'orientation du panneau : on ajoute 10 blocs dans une direction donn�e 
			
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
				p.teleport(nextLocation); // t�l�porter le joueur au nouvel emplacement
			});
		}
	}
	
	/**
	 * Permet d'obtenir tous les joueurs dans un rayon de 5,5 blocks (reach d'un joueur lambda)
	 * @param player -> Joueur qui a cliqu�
	 * @return Collection de joueurs qui contient tous les joueurs autour du joueur qui � cliqu� dans un rayon de 5,5 block + le joueur ayant cliqu�
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Player> getAllNearPlayers(Player player) {
		return (Collection) player.getWorld().getNearbyEntities(player.getLocation(), 5.5d, 5.5d, 5.5d, e -> (e instanceof Player));
	}

}

enum DongeonSignTypes {
	
	STARTER("STATE.START"), // Panneau de lancement
	STOPPER("STATE.STOP"); // Panneau d'arr�t (Note du dev : non-utilis� pour l'instant)
	
	private String systemKey; // Variable de stockage de la cl� syst�me
	
	/**
	 * Constructeur pour stocker la cl� syst�me
	 * @param systemKey -> cl� syst�me
	 */
	private DongeonSignTypes(String systemKey) {
		this.systemKey = systemKey;
	}
	
	/**
	 * Permet de r�cup�rer la cl� syst�me qui sera �crite sur le panneau en premi�re ligne (soit lines[0])
	 * @return String -> Cl� syst�me
	 */
	public String systemKey() {
		return this.systemKey;
	}
 	
}

