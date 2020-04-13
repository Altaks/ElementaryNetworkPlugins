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
	
	private List<Material> systemSignTypes = new ArrayList<Material>(); // Liste des mat�riaux des panneaux syst�mes
	private Location minLoc, maxLoc; // Location minimal et maximale du donjon. Correspond aux sommets oppos�s en diagonale de la zone
	{
		// On rajoute les mat�riaux ACACIA_SIGN et ACACIA_WALL_SIGN aux panneaux syst�mes
		Material[] systemSignTypes = { Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN }; 
		this.systemSignTypes.addAll(Arrays.asList(systemSignTypes));
	}
	
	/**
	 * Constructeur qui permet d'obtenir un DungeonArea ({@link fr.altaks.eodungeons.core.DongeonArea}) qui permet de d�limiter la zone d'un donjon
	 * @param minLoc -> Location minimale de la zone
	 * @param maxLoc -> Location maximale de la zone
	 */
	public DongeonArea(Location minLoc, Location maxLoc) {
		this.minLoc = minLoc; this.maxLoc = maxLoc;
	}
	
	/**
	 * M�thode qui permet de savoir s'il reste une entit� non-joueur dans la zone
	 * @return boolean -> est ce qu'il reste une entit� non-joueur dans la zone
	 */
	public boolean isAliveNonPlayerEntityInWholeArea() {
		World world = minLoc.getWorld(); // On r�cup le monde concern�
		
		Chunk minChunk = minLoc.getChunk(); // On r�cup�re le Chunk minimal
		Chunk maxChunk = maxLoc.getChunk(); // On r�cup�re le Chunk maximal
		
		int minX = minChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk minimal
		int minZ = minChunk.getZ();	// On r�cup les coordoon�es Y de chunk du chunk minimal
		
		int maxX = maxChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk maximal
		int maxZ = maxChunk.getZ(); // On r�cup les coordoon�es Y de chunk du chunk maximal
		
		List<Entity> entities = new ArrayList<>(); // On cr�� une liste d'entit�s vide
		
		for (int x = minX; x <= maxX + 1; x++) { // pour chaque X de chunk entre minX et maxX
			for(int z = minZ; z <= maxZ + 1; z++) { // pour chaque Z de chunk entre minZ et maxZ
				Arrays.asList(world.getChunkAt(x, z).getEntities()).forEach(entity -> { // Pour chaque entit� dans le chunk aux coordonn�es de chunk x,z ->
					if(!(entity instanceof Player)) { // Si l'entit� n'est pas un joueur
						entities.add(entity); // Ajouter l'entit� dans la liste des entit�s de la zone
					}
				});
			}
		}
		
		if(entities.isEmpty()) return false; // Si la liste est vide, renvoyer qu'il ne reste plus d'entit� en vie dans la zone (renvoyer false)
		
		boolean isAliveEntity = false; // On cr�� une variable isAliveEntity plac�e de base sur false
		for(Entity entity : entities) { // pour chaque entit� de la liste d'entit�s dans la zone
			if(!entity.isDead()) { // Si l'entit� n'est pas morte alors
				isAliveEntity = true; // On met isAliveEntity sur true
				break; // On casse la boucle
			} else continue; // Sinon on passe � l'entit� suivante
		}
		
		if(isAliveEntity) return true; // S'il reste une entit� vivante alors on renvoie true
		return false; // On renvoie par d�faut qu'il ne reste plus d'entit� vivante (renvoyer false) 
	}
	
	/**
	 * Permet de r�cup�rer tous les panneaux syst�me d'une zone, de fa�on tri�e
	 * @return Sign[] -> Tableau contenant tous les panneaux dans un ordre d�fini.
	 */
	public Sign[] getSystemSignsFromArea() {
		World world = minLoc.getWorld(); // On r�cup�re le monde � analyser
		
		Chunk minChunk = minLoc.getChunk(); // On r�cup�re le Chunk minimal
		Chunk maxChunk = maxLoc.getChunk(); // On r�cup�re le Chunk maximal
		
		int minX = minChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk minimal
		int minZ = minChunk.getZ();	// On r�cup les coordoon�es Y de chunk du chunk minimal
		
		int maxX = maxChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk maximal
		int maxZ = maxChunk.getZ(); // On r�cup les coordoon�es Y de chunk du chunk maximal
		
		List<BlockState> signEntities = new ArrayList<BlockState>(); // On cr�� une liste de TileEntity (BlockState) vide
		
		for (int x = minX; x <= maxX + 1; x++) { // pour chaque X de chunk entre minX et maxX
			for(int z = minZ; z <= maxZ + 1; z++) { // pour chaque Z de chunk entre minZ et maxZ
				Arrays.asList(world.getChunkAt(x, z).getTileEntities()) // On r�cup�re la liste des TileEntities de la zone
				.stream() // On les passe en Steam()
				.filter(tile -> this.systemSignTypes.contains(tile.getBlock().getType())) // On garde uniquement les panneaux dont le type fait partie des type de panneaux syst�me (proc�dure de filtrage)
				.forEach(sign -> signEntities.add(sign)); // pour chaque TileEntity (qui est gard�e apr�s le filtrage) -> on ajoute la TileEntity a la liste des TileEntity qui �tait vide
			}
		}
		
		Sign[] signs = signEntities.toArray(new Sign[signEntities.size()]); // On transforme la liste des panneaux en tableau
		SortingUtil.signBubbleSort(signs); // On trie le panneau via un algorithme de triage. �a va mettre dans l'ordre les panneaux en fonction de leur num�ro de vague
		return signs; // On renvoie le tableau de panneau tri�
	}

}
