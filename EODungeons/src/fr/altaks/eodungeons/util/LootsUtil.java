package fr.altaks.eodungeons.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.core.DungeonLoots;
import net.minecraft.server.v1_15_R1.MojangsonParser;
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
	public static DungeonLoots getDungeonLoots(Main main, File winLoots, File looseLoots, int groupSize) {
		ArrayList<ItemStack> win_items = getItems(winLoots, groupSize);
		ArrayList<ItemStack> fail_items = getItems(looseLoots, groupSize);
		return new DungeonLoots(main, win_items, fail_items);
	}
	
	/**
	 * Permet de lire tous les items d'un fichier
	 * @param file -> Fichier qui renferme tous les items
	 * @return List<ItemStack> qui contient tous les items inscrits dans le fichier
	 */
	private static ArrayList<ItemStack> getItems(File file, int groupSize){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
		for(String item : yml.getKeys(false)) {
			ItemStack i = getItem(yml.getConfigurationSection(item));
			if(yml.isSet(item + ".loot-luck")) {
				
				if(yml.get(item + ".loot-luck") instanceof String){
					double percentage = getPersentageFromString(yml.getString(item + ".loot-luck"), groupSize);
					if(new Random().nextDouble() * 100 < percentage) {
						items.add(i);
					}
				} else if(yml.get(item + ".loot-luck") instanceof Double || yml.get(item + ".loot-luck") instanceof Integer) {
					if(new Random().nextDouble() * 100 < yml.getConfigurationSection(item).getDouble("loot-luck")) {
						items.add(i);
					}
				}
			} else items.add(i);
		}
		return items;
	}

	private static double getPersentageFromString(String str, int groupSize) {
		return Double.parseDouble(str.replace("[", "").replace("]", "").split("/")[groupSize - 1]);
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
			try {
				compound = MojangsonParser.parse(compoundString);
				compound.setString("id", itemNameSpacedKey);
				compound.setInt("amount", stackAmount);
				nbtTagNotNull = true;
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
		}
		
		Material material = Material.AIR;
		
		for(Material m : Material.values()) {
			if(m.getKey().toString().equalsIgnoreCase(itemNameSpacedKey)) {
				material = m;
				break;
			}
		}
		
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
