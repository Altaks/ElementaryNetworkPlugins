package fr.altaks.eodungeons.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import fr.altaks.eodungeons.Main;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

/**
 * @author Altaks
 */
public class Wave {
	
	/**
	 * Liste des mobs contenus dans la vague
	 */
	public List<WaveMob> waveMobs = new ArrayList<Wave.WaveMob>();
	
	/**
	 * Constructeur qui va initialiser tous les mobs de l'ArrayList, permet de stocker les vagues
	 * @see fr.altaks.eodungeons.core.Wave
	 * @param mobs -> Liste des Mobs de la vague
	 */
	public Wave(List<WaveMob> mobs) {
		this.waveMobs = mobs; // Pour chaque mob de la liste, l'ajouter � la liste de "this" et non celle du constructeur
	}
	
	/**
	 * Fonction qui permet de lancer la vague.
	 */
	public void start(Location signLocation) {
		this.getMobs().forEach(mob -> mob.summon(signLocation)); // Pour chaque mob de la vague, le faire spawner
	}
	
	/**
	 * Fonction statique permettant de charger une vague depuis un fichier et l'int�grer � son donjon
	 * @param dungeon -> Donjon qui utilisera cette vague
	 * @param main -> Classe Main du plugin
	 * @param file -> Fichier qui va contenir la vague
	 * @return Wave -> la vague inscrite dans le fichier
	 * @throws NullPointerException -> Peut provoquer une NullPointerException si le fichier est mal �crit/introuvable
	 * @throws IOException -> Peut provoquer une IOException si le fichier est introuvable
	 */
	@SuppressWarnings("deprecation")
	public static Wave loadWaveFromYmlFile(Dongeon dungeon, Main main, File file) throws NullPointerException, IOException {
		
		if(!file.exists()) return null; // Si le fichier n'existe pas, provoquer une NullPointerException
		if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Liste \"" + file.getName() + "\" en cours de load"));
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file); // On charge le fichier en tant que FileConfiguration
		if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Fichier yaml " + yml.getName() + " charg�"));
		List<WaveMob> mobs = new ArrayList<Wave.WaveMob>(); // On cr�� une liste de mobs vide qui sera celle de la vague.
		
		for(String entity : yml.getConfigurationSection("entities").getKeys(false)) { // Pour chaque entit�
			
			int spawnTimes = yml.getInt("entities." + entity + ".spawn-times");
			
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�e\u00BB Mob en cours de load spawnera " + spawnTimes + " fois"));

			// Obtenir le type d'entit� depuis MC
			EntityType baseEntityType = EntityType.FOX;
			String baseEntityTypeKey = yml.getString("entities." + entity + ".entity-type");
			baseEntityType = EntityType.fromName(baseEntityTypeKey.split(":")[1]);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�e\u00BB Mob en cours de load sera : " + baseEntityTypeKey));
			
			// Obtenir le type de mob : Est ce un mob, boss ou worldboss ?
			DungeonMobType mobType = DungeonMobType.MOB;
			String mobTypeKey = yml.getString("entities." + entity + ".dongeon-mob-type");
			mobType = DungeonMobType.getByConfigKey(mobTypeKey);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Mob en cours de load sera un mob de donjon : " + mobTypeKey));
			
			// Obtenir la vie de l'entit�
			double health = yml.getDouble("entities." + entity + ".health");
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Mob en cours de load aura " + health + "pv"));
			
			// Obtenir le NBTTag qui sera appliqu� � l'entit�
			String nbtTag = yml.getString("entities." + entity + ".nbt-tag");
			NBTTagCompound compound = new NBTTagCompound().getCompound(nbtTag);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Mob en cours de load aura le tag : " + nbtTag));
			
			// Obtenir l'inventaire :
			
				// Obtenir l'armure :
			
					// Obtenir le casque :
			
					ItemStack helmet = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.helmet"));
			
					// Obtenir le plastron :
					
					ItemStack chestplate = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.chestplate"));
					
					// Obtenir les jambi�res :
					
					ItemStack leggings = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.leggings"));
					
					// Obtenir les bottes :
					
					ItemStack boots = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.boots"));
				
				// Obtenir le contenu des mains :
					
					// Obtenir la main principale : 
					
					ItemStack mainhand = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.hands.main-hand"));
					
					// Obtenir la main secondaire : 
					
					ItemStack offhand = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.hands.off-hand"));
				
			// On stocke l'inventaire dans une classe MobInventory
			MobInventory inventory = new MobInventory(helmet, chestplate, leggings, boots, mainhand, offhand);
			
			// On ajoute le mob lu dans la liste de la vague
			mobs.add(new WaveMob(dungeon, main, spawnTimes, baseEntityType, mobType, health, inventory, compound));
		}
		// On renvoie la vague
		return new Wave(mobs);
	}
	
	// Permet de r�cup�rer la liste des mobs de cette vague
	public List<WaveMob> getMobs(){
		return this.waveMobs;
	}
	
	/**
	 * Classe int�gr�e dans Wave, permet de facilement g�r�r/cr�er un mob de donjon
	 * @author Altaks
	 */
	private static class WaveMob {
		
		private int spawnTimes; // Liste des endroits ou va spawn le mob
		private EntityType basicEntityType; // Type d'entit� vanilla (entit� directe de MC)
		private DungeonMobType mobEntityType; // Type de mob de donjon (mob, miniboss ou worldboss)
		private double health = 1d; // Vie du mob, mise par d�faut � 0.5 coeurs
		private MobInventory inventory; // Inventaire du mob
		private NBTTagCompound nbtTag; // Tag du mob
		private Main main; // Classe Main du plugin
		private Dongeon dongeon; // Donjon dans lequel le mob va spawn
		
		/**
		 * Constructeur du mob de donjon (il ne spawn pas, il est juste pseudo-charg�)
		 * @param dungeon -> Donjon associ� au mob (dans lequel il va spawn)
		 * @param main -> Classe Main du plugin
		 * @param spawnLocations -> Endroits ou va spawner le mob
		 * @param basicEntityType -> Type d'entit� vanilla
		 * @param mobEntityType -> Type de mob de donjon
		 * @param health -> Vie maximale et de d�part du mob
		 * @param inventory -> Inventaire du mob
		 * @param nbtTag -> NBTTag qui va �tre appliqu� au mob
		 */
		public WaveMob(Dongeon dungeon, Main main, int spawnTimes, EntityType basicEntityType, DungeonMobType mobEntityType, double health, MobInventory inventory, NBTTagCompound nbtTag) {
			this.spawnTimes = spawnTimes;
			
			this.basicEntityType = basicEntityType;
			this.mobEntityType = mobEntityType;
			
			this.inventory = inventory;
			
			nbtTag.setString("id", basicEntityType.getKey().toString());
			this.nbtTag = nbtTag;
			
			this.dongeon = dungeon;
			this.main = main;
		}
		
		public Dongeon getDungeon() {
			return this.dongeon;
		}
		
		/**
		 * M�thode qui va faire appara�tre le mob � chaque de ses positions d'apparition.
		 */
		@SuppressWarnings("deprecation")
		public void summon(Location signLocation) {
			if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Mob en cours de spawn"));
			
			signLocation = signLocation.add(0, 2, 0);
			
			for(int i = 0; i < this.spawnTimes; i++) {
				org.bukkit.entity.Entity entity = signLocation.getWorld().spawn(signLocation, basicEntityType.getEntityClass());
				
				if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Entit� de type " + basicEntityType.getKey().toString() + " spawn�e"));
				
				if((entity instanceof LivingEntity)) { // Si l'entit� est une entit� vivante et que ce n'est pas un joueur :
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Entit� de type vivante"));
					
					((CraftEntity)entity).getHandle().d(nbtTag); // On r�cup�re l'entit� en NMS et on applique le tag
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB NBTTag appliqu� � l'entit�"));
					
					LivingEntity livingEntity = (LivingEntity) entity; // on r�cup�re l'entit� en tant qu'entit� vivante (LivingEntity)
					
					livingEntity.setMaxHealth(health); // On set la vie maximale de l'entit� au niveau d�sign� par le constructeur 
					livingEntity.setHealth(health); // On set la vie actuelle de l'entit� au niveau d�sign� par le constructeur
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Vie appliqu�e � l'entit�"));
					
					EntityEquipment equipement = ((LivingEntity) entity).getEquipment(); // On r�cup�re l'�quipement de l'entit�
					
					// On place l'armure du mob dans ses slots
					equipement.setHelmet(inventory.getHelmet());
					equipement.setChestplate(inventory.getChestplate());
					equipement.setLeggings(inventory.getLeggings());
					equipement.setBoots(inventory.getBoots());
					
					// On place les items destin�s aux mains dans leurs slots
					equipement.setItemInMainHand(inventory.getMainHand());
					equipement.setItemInOffHand(inventory.getOffHand());
					
					// On d�sactive les chances de drop de l'armure
					equipement.setHelmetDropChance(0f);
					equipement.setChestplateDropChance(0f);
					equipement.setLeggingsDropChance(0f);
					equipement.setBootsDropChance(0f);
					
					// On d�sactive les chances de drop des items plac�s dans les mains
					equipement.setItemInMainHandDropChance(0f);
					equipement.setItemInOffHandDropChance(0f);
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Equipement appliqu� � l'entit�"));
					
					((LivingEntity) entity).setRemoveWhenFarAway(false); // On d�sactive le dispawn naturel de l'entit�
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Dispawn naturel d�sactiv�"));
					
					main.getActiveEntityIDs().add(entity.getUniqueId()); // On rajoute dans le main cette entit� en tant qu'entit� spawn�e par un donjon
					if(this.mobEntityType.equals(DungeonMobType.BOSS) || this.mobEntityType.equals(DungeonMobType.WORLDBOSS)) { // Si ce mob est un boss/worldboss alors :
						main.getSpawnedBossesIDs().put(entity.getUniqueId(), this.dongeon); // On place cette entit� en liason avec le donjon dans la HashMap des boss spawn�es par des donjons
					}
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Entit� compl�t�e"));

				} else {
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("�c\u00BB Entit� non-vivante"));
				}
			}
			
		}
		
	}
	
	/**
	 * Classe permettant de mieux g�rer les inventaires des mobs
	 * @author Altaks
	 */
	private static class MobInventory {
		
		/**
		 * Items contenus dans l'inventaire
		 */
		private ItemStack helmet, chestplate, leggings, boots, mainhand, offhand;
		
		/**
		 * Constructeur de MobInventory
		 * @param helmet -> ItemStack qui correspond au casque
		 * @param chestplate -> ItemStack qui correspond au plastron
		 * @param leggings -> ItemStack qui correspond aux jambi�res
		 * @param boots -> ItemStack qui correspond aux bottes
		 * @param mainhand -> ItemStack qui correspond � la main principale
		 * @param offhand -> ItemStack qui correspond � la main secondaire
		 */
		public MobInventory(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainhand, ItemStack offhand) {
			this.helmet = helmet;
			this.chestplate = chestplate;
			this.leggings = leggings;
			this.boots = boots;
			
			this.mainhand = mainhand;
			this.offhand = offhand;
			
		}
		
		/**
		 * M�thode pour r�cup�rer le casque contenu dans l'inventaire
		 * @return ItemStack -> Casque
		 */
		public ItemStack getHelmet() {
			return this.helmet;
		}
		
		/**
		 * M�thode pour r�cup�rer le plastron contenu dans l'inventaire
		 * @return ItemStack -> Plastron
		 */
		public ItemStack getChestplate() {
			return this.chestplate;
		}
		
		/**
		 * M�thode pour r�cup�rer les jambi�res contenues dans l'inventaire
		 * @return ItemStack -> Jambi�res
		 */
		public ItemStack getLeggings() {
			return this.leggings;
		}
		
		/**
		 * M�thode pour r�cup�rer les bottes contenues dans l'inventaire
		 * @return ItemStack -> Bottes
		 */
		public ItemStack getBoots() {
			return this.boots;
		}
		
		/**
		 * M�thode pour r�cup�rer l'objet de la main principale
		 * @return ItemStack -> Item de la main pricipale
		 */
		public ItemStack getMainHand() {
			return this.mainhand;
		}
		
		/**
		 * M�thode pour r�cup�rer l'objet de la main secondaire
		 * @return ItemStack -> Item de la main secondaire
		 */
		public ItemStack getOffHand() {
			return this.offhand;
		}
		
	}
	
	/**
	 * M�thode permettant de lire un item avec NBTTag depuis une ConfigurationSection d'une FileConfiguration
	 * @param section -> Section de FileConfiguration s�l�ctionn�e
	 * @return ItemStack -> Item qui est lu dans la ConfigurationSection
	 */
	public static ItemStack itemReader(ConfigurationSection section) {
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
		
		Material material = Material.RED_NETHER_BRICKS;
		
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


