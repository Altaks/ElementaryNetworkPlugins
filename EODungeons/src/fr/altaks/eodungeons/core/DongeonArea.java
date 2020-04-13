package fr.altaks.eodungeons.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.altaks.eodungeons.util.SortingUtil;

/**
 * @author Altaks
 */
public class DongeonArea { 
	
	private List<Material> systemSignTypes = new ArrayList<Material>(); // Liste des matériaux des panneaux systèmes
	private Location minLoc, maxLoc; // Location minimal et maximale du donjon. Correspond aux sommets opposés en diagonale de la zone
	{
		// On rajoute les matériaux ACACIA_SIGN et ACACIA_WALL_SIGN aux panneaux systèmes
		Material[] systemSignTypes = { Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN }; 
		this.systemSignTypes.addAll(Arrays.asList(systemSignTypes));
	}
	
	/**
	 * Constructeur qui permet d'obtenir un DungeonArea ({@link fr.altaks.eodungeons.core.DongeonArea}) qui permet de délimiter la zone d'un donjon
	 * @param minLoc -> Location minimale de la zone
	 * @param maxLoc -> Location maximale de la zone
	 */
	public DongeonArea(Location minLoc, Location maxLoc) {
		this.minLoc = minLoc; this.maxLoc = maxLoc;
	}
	
	/**
	 * Méthode qui permet de savoir s'il reste une entité non-joueur dans la zone
	 * @return boolean -> est ce qu'il reste une entité non-joueur dans la zone
	 */
	public boolean isAliveNonPlayerEntityInWholeArea() {
		World world = minLoc.getWorld(); // On récup le monde concerné
		
		Chunk minChunk = minLoc.getChunk(); // On récupère le Chunk minimal
		Chunk maxChunk = maxLoc.getChunk(); // On récupère le Chunk maximal
		
		int minX = minChunk.getX(); // On récup les coordoonées X de chunk du chunk minimal
		int minZ = minChunk.getZ();	// On récup les coordoonées Y de chunk du chunk minimal
		
		int maxX = maxChunk.getX(); // On récup les coordoonées X de chunk du chunk maximal
		int maxZ = maxChunk.getZ(); // On récup les coordoonées Y de chunk du chunk maximal
		
		List<Entity> entities = new ArrayList<>(); // On créé une liste d'entités vide
		
		for (int x = minX; x <= maxX + 1; x++) { // pour chaque X de chunk entre minX et maxX
			for(int z = minZ; z <= maxZ + 1; z++) { // pour chaque Z de chunk entre minZ et maxZ
				Arrays.asList(world.getChunkAt(x, z).getEntities()).forEach(entity -> { // Pour chaque entité dans le chunk aux coordonnées de chunk x,z ->
					if(!(entity instanceof Player)) { // Si l'entité n'est pas un joueur
						entities.add(entity); // Ajouter l'entité dans la liste des entités de la zone
					}
				});
			}
		}
		
		if(entities.isEmpty()) return false; // Si la liste est vide, renvoyer qu'il ne reste plus d'entité en vie dans la zone (renvoyer false)
		
		boolean isAliveEntity = false; // On créé une variable isAliveEntity placée de base sur false
		for(Entity entity : entities) { // pour chaque entité de la liste d'entités dans la zone
			if(!entity.isDead()) { // Si l'entité n'est pas morte alors
				isAliveEntity = true; // On met isAliveEntity sur true
				break; // On casse la boucle
			} else continue; // Sinon on passe à l'entité suivante
		}
		
		if(isAliveEntity) return true; // S'il reste une entité vivante alors on renvoie true
		return false; // On renvoie par défaut qu'il ne reste plus d'entité vivante (renvoyer false) 
	}
	
	/**
	 * Permet de récupérer tous les panneaux système d'une zone, de façon triée
	 * @return Sign[] -> Tableau contenant tous les panneaux dans un ordre défini.
	 */
	public Sign[] getSystemSignsFromArea() {
		World world = minLoc.getWorld(); // On récupère le monde à analyser
		
		Chunk minChunk = minLoc.getChunk(); // On récupère le Chunk minimal
		Chunk maxChunk = maxLoc.getChunk(); // On récupère le Chunk maximal
		
		int minX = minChunk.getX(); // On récup les coordoonées X de chunk du chunk minimal
		int minZ = minChunk.getZ();	// On récup les coordoonées Y de chunk du chunk minimal
		
		int maxX = maxChunk.getX(); // On récup les coordoonées X de chunk du chunk maximal
		int maxZ = maxChunk.getZ(); // On récup les coordoonées Y de chunk du chunk maximal
		
		List<BlockState> signEntities = new ArrayList<BlockState>(); // On créé une liste de TileEntity (BlockState) vide
		
		for (int x = minX; x <= maxX + 1; x++) { // pour chaque X de chunk entre minX et maxX
			for(int z = minZ; z <= maxZ + 1; z++) { // pour chaque Z de chunk entre minZ et maxZ
				Arrays.asList(world.getChunkAt(x, z).getTileEntities()) // On récupère la liste des TileEntities de la zone
				.stream() // On les passe en Steam()
				.filter(tile -> this.systemSignTypes.contains(tile.getBlock().getType())) // On garde uniquement les panneaux dont le type fait partie des type de panneaux système (procédure de filtrage)
				.forEach(sign -> signEntities.add(sign)); // pour chaque TileEntity (qui est gardée après le filtrage) -> on ajoute la TileEntity a la liste des TileEntity qui était vide
			}
		}
		
		Sign[] signs = signEntities.toArray(new Sign[signEntities.size()]); // On transforme la liste des panneaux en tableau
		SortingUtil.signBubbleSort(signs); // On trie le panneau via un algorithme de triage. ça va mettre dans l'ordre les panneaux en fonction de leur numéro de vague
		return signs; // On renvoie le tableau de panneau trié
	}

}
