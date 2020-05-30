package fr.altaks.eodungeons.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class GetItemTagCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && sender.hasPermission("itemtag.get.use") && cmd.getName().equalsIgnoreCase("getitemtag")) {
			Player player = (Player)sender;
			
			if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
				player.sendMessage(Main.PLUGIN_PREFIX + "Vous n'avez aucun item dans la main");
				return true;
			} else {
				ItemStack item = player.getInventory().getItemInMainHand();
				String nbtTag = CraftItemStack.asNMSCopy(item).getTag().toString();
				TextComponent comp = new TextComponent(Main.PLUGIN_PREFIX + "Cliquez ici pour stocker le NBT de cet item dans votre presse-papier");
				comp.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, nbtTag.replace("\"", "\\\"")));
				player.spigot().sendMessage(comp);
				return true;
			}
		}
		
		
		return false;
	}

}
