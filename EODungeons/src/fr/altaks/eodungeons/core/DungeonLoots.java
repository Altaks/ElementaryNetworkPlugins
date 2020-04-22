package fr.altaks.eodungeons.core;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.Main;

/**
 * @author Altaks
 */
public class DungeonLoots {
	
	/**
	 * Liste des items gagnés en cas de victoire
	 */
	private ArrayList<ItemStack> win_loots;
	
	/**
	 * Liste des items gagnés en cas de défaite
	 */
	private ArrayList<ItemStack> loose_loots;
	
	/**
	 * Constructeur permettant d'obtenir {@link fr.altaks.eodungeons.core.DungeonLoots} qui permet de mieux gérer les loots d'un donjon
	 * @param main -> Classe main du plugin
	 * @param win_loots -> liste des items en cas de victoire
	 * @param loose_loots -> liste des items en cas de défaite
	 */
	public DungeonLoots(Main main, ArrayList<ItemStack> win_loots, ArrayList<ItemStack> loose_loots) {
		this.win_loots = win_loots; // pour chaque item dans la liste d'items de victoire donnée -> ajouter l'item dans la liste d'items de victoire de la classe
		this.loose_loots = loose_loots; // pour chaque item dans la liste d'items de défaite donnée -> ajouter l'item dans la liste d'items de défaite de la classe
	}
	
	/**
	 * Permet de récupérer les items de victoire
	 * @return List<ItemStack> -> Items en cas de victoire
	 */
	public ArrayList<ItemStack> getWinLoots(){
		return this.win_loots;
	}
	
	/**
	 * Permet de récupérer les items en cas de défaite
	 * @return List<ItemStack> -> Items en cas de défaite
	 */
	public ArrayList<ItemStack> getLooseLoots(){
		return this.loose_loots;
	}
}
