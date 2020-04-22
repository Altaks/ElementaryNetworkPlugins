package fr.altaks.eodungeons.util;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.core.DungeonLoots;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

/**
 * @author Altaks
 */
public class LootsUtil {
	
	/**
	 * permet d'obtenir un DungeonLoot depuis 2 fichiers
	 * @param main -> correspond à la classe Main du plugin
	 * @param winLoots -> Fichier qui contient la liste des items qui vont être donnés en cas de victoire
	 * @param looseLoots -> Fichier qui contient la liste des items qui vont être donnés en cas d'échec
	 * @return DungeonLoots
	 */
	public static DungeonLoots getDungeonLoots(Main main, File winLoots, File looseLoots) {
		ArrayList<ItemStack> win_items = getItems(winLoots);
		ArrayList<ItemStack> fail_items = getItems(looseLoots);
		return new DungeonLoots(main, win_items, fail_items);
	}
	
	/**
	 * Permet de lire tous les items d'un fichier
	 * @param file -> Fichier qui renferme tous les items
	 * @return List<ItemStack> qui contient tous les items inscrits dans le fichier
	 */
	private static ArrayList<ItemStack> getItems(File file){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
		for(String item : yml.getKeys(false)) {
			items.add(getItem(yml.getConfigurationSection(item)));
		}
		return items;
	}
	
	/**
	 * Permet d'obtenir un item depuis une section d'une FileConfiguration
	 * @param section -> Section d'une FileConfiguration, qui va contenir l'item
	 * @return ItemStack -> renvoie l'objet lu par le code avec un NBTTag appliqué
	 */
	private static ItemStack getItem(ConfigurationSection section) {
		String itemNameSpacedKey = section.getString("item-name");
		String compoundString = section.getString("nbt-tag");
		int stackAmount = section.getInt("amount");
		
		boolean nbtTagNotNull = false;
		NBTTagCompound compound = new NBTTagCompound();
		if(!(compoundString.equalsIgnoreCase("{}") || compoundString.equalsIgnoreCase(""))) {
			compound = new NBTTagCompound().getCompound(compoundString);
			compound.setString("id", itemNameSpacedKey);
			compound.setInt("amount", stackAmount);
			nbtTagNotNull = true;
		}
		
		Material material = Material.getMaterial(itemNameSpacedKey.split(":")[1]);
		
		ItemStack itemBase = new ItemStack(material, stackAmount);
		if(nbtTagNotNull) {
			net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemBase);
			nmsItem.setTag(compound);
			
			ItemStack finalItem = CraftItemStack.asBukkitCopy(nmsItem);
			return finalItem;
		}
		return itemBase;
	}

}
