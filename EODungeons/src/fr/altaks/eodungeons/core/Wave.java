package fr.altaks.eodungeons.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

/**
 * @author Altaks
 */
public class Wave {
	
	/**
	 * Liste des mobs contenus dans la vague
	 */
	public List<WaveMob> waveMobs = new ArrayList<Wave.WaveMob>();
	private boolean isLaunched = false;
	
	/**
	 * Constructeur qui va initialiser tous les mobs de l'ArrayList, permet de stocker les vagues
	 * @see fr.altaks.eodungeons.core.Wave
	 * @param mobs -> Liste des Mobs de la vague
	 */
	public Wave(List<WaveMob> mobs) {
		this.waveMobs = mobs; // Pour chaque mob de la liste, l'ajouter à la liste de "this" et non celle du constructeur
	}
	
	/*
	 * Permet de savoir si la vague est lancée
	 */
	public boolean isLaunched() {
		return this.isLaunched;
	}
	
	/**
	 * Fonction qui permet de lancer la vague.
	 */
	public void start() {
		this.getMobs().forEach(mob -> mob.summon()); // Pour chaque mob de la vague, le faire spawner
		this.isLaunched = true;
	}
	
	/**
	 * Fonction statique permettant de charger une vague depuis un fichier et l'intégrer à son donjon
	 * @param dungeon -> Donjon qui utilisera cette vague
	 * @param main -> Classe Main du plugin
	 * @param file -> Fichier qui va contenir la vague
	 * @return Wave -> la vague inscrite dans le fichier
	 * @throws NullPointerException -> Peut provoquer une NullPointerException si le fichier est mal écrit/introuvable
	 * @throws IOException -> Peut provoquer une IOException si le fichier est introuvable
	 */
	@SuppressWarnings("deprecation")
	public static Wave loadWaveFromYmlFile(Dongeon dungeon, Main main, File file) throws NullPointerException, IOException {
		
		if(!file.exists()) return null; // Si le fichier n'existe pas, provoquer une NullPointerException
		if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Liste \"" + file.getName() + "\" en cours de load"));
		FileConfiguration yml = YamlConfiguration.loadConfiguration(file); // On charge le fichier en tant que FileConfiguration
		if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Fichier yaml " + yml.getName() + " chargé"));
		List<WaveMob> mobs = new ArrayList<Wave.WaveMob>(); // On créé une liste de mobs vide qui sera celle de la vague.
		
		for(String entity : yml.getConfigurationSection("entities").getKeys(false)) { // Pour chaque entité
			
			// Obtenir la liste des endroits de spawn du mob
			List<Location> spawnLocations = new ArrayList<Location>();
			for(String location : yml.getConfigurationSection("entities." + entity + ".spawn-coordinates").getKeys(false)) {
				spawnLocations.add(getLocationFromYmlString(yml.getString("entities." + entity + ".spawn-coordinates."+ location)));
			}
			
			if(Main.isDebugging) {
				StringBuilder builder = new StringBuilder();
				spawnLocations.forEach(loc -> builder.append("\n> x: " + loc.getBlockX() + "/y:" + loc.getBlockY() + "/z:" + loc.getBlockZ()));
				dungeon.getActivePlayers().forEach(p -> p.sendMessage("§e\u00BB Mob en cours de load spawnera à : " + builder.toString()));
			}
			
			// Obtenir le type d'entité depuis MC
			EntityType baseEntityType = EntityType.FOX;
			String baseEntityTypeKey = yml.getString("entities." + entity + ".entity-type");
			baseEntityType = EntityType.fromName(baseEntityTypeKey.split(":")[1]);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§e\u00BB Mob en cours de load sera : " + baseEntityTypeKey));
			
			// Obtenir le type de mob : Est ce un mob, boss ou worldboss ?
			DungeonMobType mobType = DungeonMobType.MOB;
			String mobTypeKey = yml.getString("entities." + entity + ".dongeon-mob-type");
			mobType = DungeonMobType.getByConfigKey(mobTypeKey);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Mob en cours de load sera un mob de donjon : " + mobTypeKey));
			
			// Obtenir la vie de l'entité
			double health = yml.getDouble("entities." + entity + ".health");
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Mob en cours de load aura " + health + "pv"));
			
			// Obtenir le NBTTag qui sera appliqué à l'entité
			String nbtTag = yml.getString("entities." + entity + ".nbt-tag");
			NBTTagCompound compound = new NBTTagCompound().getCompound(nbtTag);
			if(Main.isDebugging) dungeon.getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Mob en cours de load aura le tag : " + nbtTag));
			
			// Obtenir l'inventaire :
			
				// Obtenir l'armure :
			
					// Obtenir le casque :
			
					ItemStack helmet = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.helmet"));
			
					// Obtenir le plastron :
					
					ItemStack chestplate = itemReader(yml.getConfigurationSection("entities." + entity + ".inventory.armor.chestplate"));
					
					// Obtenir les jambières :
					
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
			mobs.add(new WaveMob(dungeon, main, spawnLocations, baseEntityType, mobType, health, inventory, compound));
		}
		// On renvoie la vague
		return new Wave(mobs);
	}
	
	// Permet de récupérer la liste des mobs de cette vague
	public List<WaveMob> getMobs(){
		return this.waveMobs;
	}
	
	// Permet de lire une Location depuis un strinc suivant le pattern [world/x/y/z/pitch/yaw]
	private static Location getLocationFromYmlString(String str) {
		str = str.replace("[", "").replace("]",""); // On retire "[" et "]" du String
		
		String[] coords = str.split("/"); // On sépare les données en tableau via les "/"
		String worldname = coords[0]; // On stocke le nom du monde
		String strX = coords[1], strY = coords[2], strZ = coords[3]; // On stocke les coordonées
		World world = Bukkit.getWorld(worldname); // On récupère le monde
		double x = Double.parseDouble(strX), y = Double.parseDouble(strY), z = Double.parseDouble(strZ); // On convertit les coords
		if(coords.length > 4) { // Si le tableau possède plus de 4 entrées soit plus que world, x, y et z, alors :
			String strPitch = coords[4], strYaw = coords[5]; // On récupère pitch et yaw en String
			float pitch = Float.parseFloat(strPitch), yaw = Float.parseFloat(strYaw); // On convertit les angles en float
			return new Location(world, x, y, z, yaw, pitch); // On renvoie la Location lue
		}
		return new Location(world, x, y, z); // On renvoie la Location lue
	}
	
	/**
	 * Classe intégrée dans Wave, permet de facilement gérér/créer un mob de donjon
	 * @author Altaks
	 */
	private static class WaveMob {
		
		private List<Location> spawnLocations; // Liste des endroits ou va spawn le mob
		private EntityType basicEntityType; // Type d'entité vanilla (entité directe de MC)
		private DungeonMobType mobEntityType; // Type de mob de donjon (mob, miniboss ou worldboss)
		private double health = 1d; // Vie du mob, mise par défaut à 0.5 coeurs
		private MobInventory inventory; // Inventaire du mob
		private NBTTagCompound nbtTag; // Tag du mob
		private Main main; // Classe Main du plugin
		private Dongeon dongeon; // Donjon dans lequel le mob va spawn
		
		/**
		 * Constructeur du mob de donjon (il ne spawn pas, il est juste pseudo-chargé)
		 * @param dungeon -> Donjon associé au mob (dans lequel il va spawn)
		 * @param main -> Classe Main du plugin
		 * @param spawnLocations -> Endroits ou va spawner le mob
		 * @param basicEntityType -> Type d'entité vanilla
		 * @param mobEntityType -> Type de mob de donjon
		 * @param health -> Vie maximale et de départ du mob
		 * @param inventory -> Inventaire du mob
		 * @param nbtTag -> NBTTag qui va être appliqué au mob
		 */
		public WaveMob(Dongeon dungeon, Main main, List<Location> spawnLocations, EntityType basicEntityType, DungeonMobType mobEntityType, double health, MobInventory inventory, NBTTagCompound nbtTag) {
			this.spawnLocations = spawnLocations;
			
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
		 * Méthode qui va faire apparaître le mob à chaque de ses positions d'apparition.
		 */
		@SuppressWarnings("deprecation")
		public void summon() {
			if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Mob en cours de spawn"));
			
			this.spawnLocations.forEach(location -> { // Pour chacune des positions d'apparition
								
				org.bukkit.entity.Entity entity = location.getWorld().spawn(location, basicEntityType.getEntityClass());
				
				if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Entité de type " + basicEntityType.getKey().toString() +" spawnée aux coordoonées x:" + location.getBlockX() + "/y:"+location.getBlockY()+"/z:"+location.getBlockZ()));
				
				if((entity instanceof LivingEntity)) { // Si l'entité est une entité vivante et que ce n'est pas un joueur :
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Entité de type vivante"));
					
					LivingEntity livingEntity = (LivingEntity) entity; // on récupère l'entité en tant qu'entité vivante (LivingEntity)
					
					livingEntity.setMaxHealth(health); // On set la vie maximale de l'entité au niveau désigné par le constructeur 
					livingEntity.setHealth(health); // On set la vie actuelle de l'entité au niveau désigné par le constructeur
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Vie appliquée à l'entité"));
					
					EntityEquipment equipement = ((LivingEntity) entity).getEquipment(); // On récupère l'équipement de l'entité
					
					// On place l'armure du mob dans ses slots
					equipement.setHelmet(inventory.getHelmet());
					equipement.setChestplate(inventory.getChestplate());
					equipement.setLeggings(inventory.getLeggings());
					equipement.setBoots(inventory.getBoots());
					
					// On place les items destinés aux mains dans leurs slots
					equipement.setItemInMainHand(inventory.getMainHand());
					equipement.setItemInOffHand(inventory.getOffHand());
					
					// On désactive les chances de drop de l'armure
					equipement.setHelmetDropChance(0f);
					equipement.setChestplateDropChance(0f);
					equipement.setLeggingsDropChance(0f);
					equipement.setBootsDropChance(0f);
					
					// On désactive les chances de drop des items placés dans les mains
					equipement.setItemInMainHandDropChance(0f);
					equipement.setItemInOffHandDropChance(0f);
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Equipement appliqué à l'entité"));

					Entity nmsEntity = ((CraftEntity)entity).getHandle(); // On récupère l'entité en NMS
					
					nmsEntity.d(nbtTag); // On place le NBTTag sur l'entité (peut override certains trucs)
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB NBTTag appliqué à l'entité"));
					
					((LivingEntity) entity).setRemoveWhenFarAway(false); // On désactive le dispawn naturel de l'entité
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Dispawn naturel désactivé"));
					
					main.getActiveEntityIDs().add(entity.getUniqueId()); // On rajoute dans le main cette entité en tant qu'entité spawnée par un donjon
					if(this.mobEntityType.equals(DungeonMobType.BOSS) || this.mobEntityType.equals(DungeonMobType.WORLDBOSS)) { // Si ce mob est un boss/worldboss alors :
						main.getSpawnedBossesIDs().put(entity.getUniqueId(), this.dongeon); // On place cette entité en liason avec le donjon dans la HashMap des boss spawnées par des donjons
					}
					
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Entité complétée"));

				} else {
					if(Main.isDebugging) getDungeon().getActivePlayers().forEach(p -> p.sendMessage("§c\u00BB Entité non-vivante"));
				}
			});
		}
		
	}
	
	/**
	 * Classe permettant de mieux gérer les inventaires des mobs
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
		 * @param leggings -> ItemStack qui correspond aux jambières
		 * @param boots -> ItemStack qui correspond aux bottes
		 * @param mainhand -> ItemStack qui correspond à la main principale
		 * @param offhand -> ItemStack qui correspond à la main secondaire
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
		 * Méthode pour récupérer le casque contenu dans l'inventaire
		 * @return ItemStack -> Casque
		 */
		public ItemStack getHelmet() {
			return this.helmet;
		}
		
		/**
		 * Méthode pour récupérer le plastron contenu dans l'inventaire
		 * @return ItemStack -> Plastron
		 */
		public ItemStack getChestplate() {
			return this.chestplate;
		}
		
		/**
		 * Méthode pour récupérer les jambières contenues dans l'inventaire
		 * @return ItemStack -> Jambières
		 */
		public ItemStack getLeggings() {
			return this.leggings;
		}
		
		/**
		 * Méthode pour récupérer les bottes contenues dans l'inventaire
		 * @return ItemStack -> Bottes
		 */
		public ItemStack getBoots() {
			return this.boots;
		}
		
		/**
		 * Méthode pour récupérer l'objet de la main principale
		 * @return ItemStack -> Item de la main pricipale
		 */
		public ItemStack getMainHand() {
			return this.mainhand;
		}
		
		/**
		 * Méthode pour récupérer l'objet de la main secondaire
		 * @return ItemStack -> Item de la main secondaire
		 */
		public ItemStack getOffHand() {
			return this.offhand;
		}
		
	}
	
	/**
	 * Méthode permettant de lire un item avec NBTTag depuis une ConfigurationSection d'une FileConfiguration
	 * @param section -> Section de FileConfiguration séléctionnée
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


