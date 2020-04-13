package fr.altaks.eodungeons.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.Main;

/**
 * @author Altaks
 */
public class DungeonLoots {
	
	/**
	 * Liste des items gagnés en cas de victoire
	 */
	private final List<ItemStack> win_loots = new ArrayList<>();
	
	/**
	 * Liste des items gagnés en cas de défaite
	 */
	private final List<ItemStack> loose_loots = new ArrayList<>();
	
	/**
	 * Constructeur permettant d'obtenir {@link fr.altaks.eodungeons.core.DungeonLoots} qui permet de mieux gérer les loots d'un donjon
	 * @param main -> Classe main du plugin
	 * @param win_loots -> liste des items en cas de victoire
	 * @param loose_loots -> liste des items en cas de défaite
	 */
	public DungeonLoots(Main main, List<ItemStack> win_loots, List<ItemStack> loose_loots) {
		win_loots.forEach(item -> this.win_loots.add(item)); // pour chaque item dans la liste d'items de victoire donnée -> ajouter l'item dans la liste d'items de victoire de la classe
		loose_loots.forEach(item -> this.loose_loots.add(item)); // pour chaque item dans la liste d'items de défaite donnée -> ajouter l'item dans la liste d'items de défaite de la classe
	}
	
	/**
	 * Permet de récupérer les items de victoire
	 * @return List<ItemStack> -> Items en cas de victoire
	 */
	public List<ItemStack> getWinLoots(){
		return this.win_loots;
	}
	
	/**
	 * Permet de récupérer les items en cas de défaite
	 * @return List<ItemStack> -> Items en cas de défaite
	 */
	public List<ItemStack> getLooseLoots(){
		return this.loose_loots;
	}
}
