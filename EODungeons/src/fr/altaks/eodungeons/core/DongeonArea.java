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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.util.SortingUtil;

/**
 * @author Altaks
 */
public class DongeonArea { 
	
	private Location minLoc, maxLoc; // Location minimal et maximale du donjon. Correspond aux sommets oppos�s en diagonale de la zone
	private Main main;
	private final List<Chunk> areaChunks = new ArrayList<Chunk>();
	
	/**
	 * Constructeur qui permet d'obtenir un DungeonArea ({@link fr.altaks.eodungeons.core.DongeonArea}) qui permet de d�limiter la zone d'un donjon
	 * @param minLoc -> Location minimale de la zone
	 * @param maxLoc -> Location maximale de la zone
	 */
	public DongeonArea(Main main, Location minLoc, Location maxLoc) {
		this.minLoc = minLoc; this.maxLoc = maxLoc;
	
		World world = minLoc.getWorld();
		
		Chunk minChunk = minLoc.getChunk(); // On r�cup�re le Chunk minimal
		Chunk maxChunk = maxLoc.getChunk(); // On r�cup�re le Chunk maximal
		
		int minX = minChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk minimal
		int minZ = minChunk.getZ();	// On r�cup les coordoon�es Y de chunk du chunk minimal
		
		int maxX = maxChunk.getX(); // On r�cup les coordoon�es X de chunk du chunk maximal
		int maxZ = maxChunk.getZ(); // On r�cup les coordoon�es Y de chunk du chunk maximal
		
		for(int x = (minX < maxX) ? minX : maxX; x <= ((minX < maxX) ? maxX : minX); x++) {
			for(int z = (minZ < maxZ) ? minZ : maxZ; z <= ((minZ < maxZ) ? maxZ : minZ); z++) {
				areaChunks.add(world.getChunkAt(x, z));
			}
		}
	}
	
	public Location getMinLoc() { return this.minLoc; }
	public Location getMaxLoc() { return this.maxLoc; }
	
	/**
	 * M�thode qui permet de savoir s'il reste une entit� non-joueur dans la zone
	 * @return boolean -> est ce qu'il reste une entit� non-joueur dans la zone
	 */
	public boolean isAliveNonPlayerEntityInWholeArea() {
		
		List<Entity> entities = new ArrayList<Entity>(); // On cr�� une liste d'entit�s vide
		
		this.areaChunks.forEach(chunk -> {
			Arrays.asList(chunk.getEntities()).forEach(entity -> {
				if((!(entity instanceof Player)) && (entity instanceof LivingEntity) && main.getActiveEntityIDs().contains(entity.getUniqueId())) { // Si l'entit� n'est pas un joueur
					entities.add(entity); // Ajouter l'entit� dans la liste des entit�s de la zone
				}
			});
		});
		
		if(entities.isEmpty()) return false; // Si la liste est vide, renvoyer qu'il ne reste plus d'entit� en vie dans la zone (renvoyer false)
		
		boolean isAliveEntity = false; // On cr�� une variable isAliveEntity plac�e de base sur false
		for(Entity entity : entities) { // pour chaque entit� de la liste d'entit�s dans la zone
			if(!entity.isDead()) { // Si l'entit� n'est pas morte alors
				isAliveEntity = true; // On met isAliveEntity sur true
				break; // On casse la boucle
			} else continue; // Sinon on passe � l'entit� suivante
		}
		
		return isAliveEntity; // S'il reste une entit� vivante alors on renvoie true sinon on renvoie false
	}
	
	/**
	 * Permet de r�cup�rer tous les panneaux syst�me d'une zone, de fa�on tri�e
	 * @return Sign[] -> Tableau contenant tous les panneaux dans un ordre d�fini.
	 */
	public Sign[] getSystemSignsFromArea() {
		
		List<BlockState> signEntities = new ArrayList<BlockState>(); // On cr�� une liste de TileEntity (BlockState) vide
		
		this.areaChunks.forEach(chunk -> {
			Arrays.asList(chunk.getTileEntities())
			.stream() // On les passe en Stream()
			.filter(tile -> (tile.getType().equals(Material.ACACIA_SIGN) || tile.getType().equals(Material.ACACIA_WALL_SIGN))) // On garde uniquement les panneaux dont le type fait partie des type de panneaux syst�me (proc�dure de filtrage)
			.forEach(sign -> signEntities.add(sign)); // pour chaque TileEntity (qui est gard�e apr�s le filtrage) -> on ajoute la TileEntity a la liste des TileEntity qui �tait vide
		});
		
		Sign[] signs = signEntities.toArray(new Sign[signEntities.size()]); // On transforme la liste des panneaux en tableau
		SortingUtil.signBubbleSort(signs); // On trie le panneau via un algorithme de triage. �a va mettre dans l'ordre les panneaux en fonction de leur num�ro de vague
		return signs; // On renvoie le tableau de panneau tri�
	}

}