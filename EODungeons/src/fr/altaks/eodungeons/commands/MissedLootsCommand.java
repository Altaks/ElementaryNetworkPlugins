package fr.altaks.eodungeons.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.util.PostDecoLootsUtil;

public class MissedLootsCommand implements CommandExecutor, Listener {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("missedloots") && sender instanceof Player) {
			
			Player player = (Player)sender;
			UUID id = player.getUniqueId();
			
			HashMap<ItemStack, Integer> items = PostDecoLootsUtil.getItemsFromPlayerList(id);
			Inventory inv = Bukkit.createInventory(null, 3 * 9, "§8\u00BB Missed Loots \u00AB");
			ItemStack[] itemTab = items.keySet().toArray(new ItemStack[items.keySet().size()]);
			
			for(int i = 0; i < inv.getSize(); i++) {
				inv.addItem(itemTab[i]);
			}
			player.openInventory(inv);
		}
		return false;
	}
	
	@EventHandler
	public void onItemPick(InventoryClickEvent event) {
		if(event.getView().getTitle().equals("§8\u00BB Missed Loots \u00AB")) {
			
			InventoryAction action = event.getAction();	
			
			if(action.equals(InventoryAction.PICKUP_ALL) || action.equals(InventoryAction.PICKUP_HALF) || action.equals(InventoryAction.PICKUP_ONE) || action.equals(InventoryAction.PICKUP_SOME)) {
				ItemStack item = event.getCurrentItem();				
				HashMap<ItemStack, Integer> items = PostDecoLootsUtil.getItemsFromPlayerList(((Player)event.getView().getPlayer()).getUniqueId());
				if(items.containsKey(item)) {
					PostDecoLootsUtil.removeItemFromPlayerList(((Player)event.getView().getPlayer()).getUniqueId(), items.get(item));
				}
			} else {
				event.setCancelled(true);
				return;
			}
		}
	}

}
