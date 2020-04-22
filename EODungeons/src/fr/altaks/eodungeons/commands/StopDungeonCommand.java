package fr.altaks.eodungeons.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import fr.altaks.eodungeons.Main;
import fr.altaks.eodungeons.core.Dongeon;

public class StopDungeonCommand implements TabExecutor {
	
	private Main main;
	
	public StopDungeonCommand(Main main) {
		this.main = main;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("stopdungeon") && sender.hasPermission("dungeon.stop.use") && args.length <= 1) {
			return new ArrayList<String>(main.getActiveDungeons().keySet());
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("stopdungeon") && sender.hasPermission("dungeon.stop.use") && args.length > 0) {
			
			String dungeonName = args[0];
			
			Dongeon dongeon = main.getActiveDungeons().get(dungeonName);
			if(dongeon == null) {
				sender.sendMessage(Main.PLUGIN_PREFIX + "Le donjon " + dungeonName + "n'est pas lancé !");
				return true;
			} else {
				dongeon.stop_loosing(true);
			}
			sender.sendMessage(Main.PLUGIN_PREFIX + "Donjon " + dungeonName + " correctement arrêté !");
			return true;
		}
		return false;
	}

}
