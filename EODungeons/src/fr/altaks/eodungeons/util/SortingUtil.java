package fr.altaks.eodungeons.util;

import org.bukkit.block.Sign;

/**
 * @author Altaks
 */
public class SortingUtil {
	
	
	/**
	 * Il s'agit de l'algorithme de triage nomm� l'algorithme � bulles, il est connu et se trouve facilement en ligne
	 * @param tab -> tableau renfermant les panneaux � trier
	 */
	public static void signBubbleSort(Sign[] tab)
	   {  
	        int taille = tab.length;  
	        Sign tmp;  
	        for(int i=0; i < taille; i++) 
	        {
	                for(int j=1; j < (taille-i); j++)
	                {  	                        
	                	    // Ici se trouve la condition dans le code qui permet de d�tecter sur quel param�tre il faut trier le tableau, en l'occurence il s'agit du num�ro de la vague situ� � la premi�re ligne du panneau, soit lines[0]
	                        if(Integer.parseInt(tab[j-1].getLines()[0].split(":")[1]) > Integer.parseInt(tab[j].getLines()[0].split(":")[1])) 
	                        {
	                                // �changes des elements
	                                tmp = tab[j-1];  
	                                tab[j-1] = tab[j];  
	                                tab[j] = tmp;  
	                        }
	 
	                }
	        }
	   }


}
