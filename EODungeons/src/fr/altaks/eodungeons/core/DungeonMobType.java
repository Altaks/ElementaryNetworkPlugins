package fr.altaks.eodungeons.core;

/**
 * @author Altaks
 */
public enum DungeonMobType {
	
	MOB("ENTITY.MOB"),
	BOSS("ENTITY.BOSS"),
	WORLDBOSS("ENTITY.WORLDBOSS");
	
	private String configKey; // Clé qui détermine le type de mob de donjon dans une config
	
	/**
	 * Constructeur de type de mob de donjon
	 * @see fr.altaks.eodungeons.core.DungeonMobType
	 * @param configKey -> Clé qui détermine le type de mob de donjon dans une config
	 */
	private DungeonMobType(String configKey) {
		this.configKey = configKey;
	}
	
	/**
	 * Permet de récup la clé de config de "this"
	 * @return String -> Clé de config
	 */
	public String getConfigKey() {
		return this.configKey;
	}
	
	/**
	 * Permet de récup le type de mob depuis une clé de config
	 * @param configKey -> Clé de config
	 * @return DungeonMobType -> Type de mob de donjon
	 */
	public static DungeonMobType getByConfigKey(String configKey) {
		for(DungeonMobType type : DungeonMobType.values()) { // Pour chaque valeur de l'énum DungeonMobType
			if(type.getConfigKey().equalsIgnoreCase(configKey)) { // Si la clé de la valeur correspont à la clé donnée alors
				return type; // On renvoie le type analysé
			}
		}
		return MOB; // Par défaut on renvoie le type mob
	}

}
