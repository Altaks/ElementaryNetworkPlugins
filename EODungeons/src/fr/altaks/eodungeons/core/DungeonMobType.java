package fr.altaks.eodungeons.core;

/**
 * @author Altaks
 */
public enum DungeonMobType {
	
	MOB("ENTITY.MOB"),
	BOSS("ENTITY.BOSS"),
	WORLDBOSS("ENTITY.WORLDBOSS");
	
	private String configKey; // Cl� qui d�termine le type de mob de donjon dans une config
	
	/**
	 * Constructeur de type de mob de donjon
	 * @see fr.altaks.eodungeons.core.DungeonMobType
	 * @param configKey -> Cl� qui d�termine le type de mob de donjon dans une config
	 */
	private DungeonMobType(String configKey) {
		this.configKey = configKey;
	}
	
	/**
	 * Permet de r�cup la cl� de config de "this"
	 * @return String -> Cl� de config
	 */
	public String getConfigKey() {
		return this.configKey;
	}
	
	/**
	 * Permet de r�cup le type de mob depuis une cl� de config
	 * @param configKey -> Cl� de config
	 * @return DungeonMobType -> Type de mob de donjon
	 */
	public static DungeonMobType getByConfigKey(String configKey) {
		for(DungeonMobType type : DungeonMobType.values()) { // Pour chaque valeur de l'�num DungeonMobType
			if(type.getConfigKey().equalsIgnoreCase(configKey)) { // Si la cl� de la valeur correspont � la cl� donn�e alors
				return type; // On renvoie le type analys�
			}
		}
		return MOB; // Par d�faut on renvoie le type mob
	}

}
