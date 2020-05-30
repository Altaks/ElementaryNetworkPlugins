package fr.altaks.eodungeons.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.altaks.eodungeons.Main;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class PostDecoLootsUtil {
	
	public static void writeNewItemToPlayerFile(UUID playerId, ItemStack... items) {
		
		File file = new File(Main.getPostDecoLootsDirectory() + File.separator + playerId.toString() + ".yml");
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		int itemIndex = 0;
		
		if(yml.getKeys(false).size() == 0) {
			itemIndex = 0;
		} else itemIndex = yml.getKeys(false).size() + 1;
		
		for(ItemStack item : items) {
			String itemPath = "item-" + itemIndex;
			writeItemInConfigSection(yml.getConfigurationSection(itemPath), item);
			itemIndex++;
		}
		
		try {
			yml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeItemInConfigSection(ConfigurationSection section, ItemStack item) {
		String material = item.getType().getKey().toString();
		String nbt = CraftItemStack.asNMSCopy(item).getTag().toString();
		int amount = item.getAmount();
		
		section.set("item-name", material);
		section.set("amount", amount);
		section.set("nbt-tag", nbt);
	}
	
	/*
	 * Integer -> itemIndex
	 * ItemStack -> item
	 */
	public static HashMap<ItemStack, Integer> getItemsFromPlayerList(UUID playerId){
		File file = new File(Main.getPostDecoLootsDirectory() + File.separator + playerId.toString() + ".yml");
		
		if(!file.exists()) return new HashMap<ItemStack, Integer>();
		HashMap<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		int itemIndex = 0;
		for(String itemSection : yml.getKeys(false)) {
			items.put(getItem(yml.getConfigurationSection(itemSection)), itemIndex);
			itemIndex++;
		}
		return items;
	}
	
	public static void removeItemFromPlayerList(UUID playerId, int itemIndex) {
		File file = new File(Main.getPostDecoLootsDirectory() + File.separator + playerId.toString() + ".yml");
		
		if(!file.exists()) return;
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
		
		yml.set("item-" + itemIndex, null);
		
		try {
			yml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
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
