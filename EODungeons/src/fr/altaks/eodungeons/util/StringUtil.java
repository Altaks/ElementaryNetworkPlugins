package fr.altaks.eodungeons.util;

import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Altaks
 */
public class StringUtil {
	
	/**
	 * Permet d'obtenir un String qui représente la barre de vie d'une entité
	 * @param currentHealth -> vie actuelle de l'entité
	 * @param maxHealth -> vie maximale de l'entité
	 * @param componentSize -> taille de la barre de vie
	 * @param fillingChar -> caractère utilisé pour remplir la barre
	 * @return TextComponent qui servira à l'ActionBar du joueur
	 */
	public static TextComponent getTextComponent(double currentHealth, double maxHealth, int componentSize, char fillingChar) {
		
		String healthcolor = "§6", deadcolor = "§7";
		StringBuilder builder = new StringBuilder(healthcolor);
		
		// CurrentLife     |    Visible Life
		// currentHealth   |        x
		//  maxHealth      |  componentSize
		
		int visiblelife = (int)((componentSize * currentHealth) / maxHealth); // calcul de la taille de la vie sur l'affichage 
		int visibledeath = componentSize - visiblelife; // calcul de la taille de la vie perdue sur l'affichage
		
		for(int i = 0 ; i < visiblelife; i++) {
			builder.append(fillingChar);
		}
		builder.append(deadcolor);
		for(int i = 0; i < visibledeath; i++) {
			builder.append(fillingChar);
		}
		builder.append("§r"); // Reset de couleur en cas de bug 
		
		return new TextComponent(builder.toString());
	}

}
