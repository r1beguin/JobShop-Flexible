import java.io.Serializable ;
import java.lang.management.GarbageCollectorMXBean ;
import java.util.HashMap ;
import org.jgrapht.graph.SimpleDirectedWeightedGraph ;

public class Operation implements Serializable {

	/* Attributs */

	private String idOperation ; 			// Les identifiants d'opération sont de la forme Ox-y où x est le numéro du Job et y le numéro de l'opération dans ce Job
	
	private HashMap<Integer, Integer> hash ; // HasMap contenant les machines pouvant exécuter cette opération et leurs temps respectifs
	
	private double soonerDate ; 			// Date de début au plus tôt 
	private double laterDate ; 				// Date de début au plus tard
	private double marge ; 					// Marge : laterDate - soonerDate


	/* Constructeur */
	public Operation(String idOperation) {

		super() ;

		this.idOperation = idOperation ; 
		
		// Initialisation de la SD, LD et marge à -1 (pour indiquer qu'elles n'ont pas encore été calculées)
		this.soonerDate = -1 ;
		this.laterDate = -1 ;
		this.marge = -1 ;
	
		this.hash = new HashMap<Integer, Integer>() ; // Initialisation à une HashMap vide
	}
	

	/* Setter */

	public void setSoonerDate(double d) {

		this.soonerDate = d ;
		this.marge = this.laterDate - this.soonerDate ; // Actualisation de la marge
	}

	public void setLaterDate(double d) {

		this.laterDate = d ;
		this.marge = this.laterDate - this.soonerDate ; // Actualisation de la marge
	}


	/* Getters */

	public String getIdOperation() 			   { return this.idOperation ; } // Getter de l'identifiant

	public HashMap<Integer, Integer> getHash() { return this.hash ; } 	     // Getter de la HashMap

	public double getSoonerDate()  			   { return this.soonerDate ; }  // Getteur de la SD

	public double getLaterDate() 			   { return this.laterDate ; }   // Getter de la LD

	public double getMarge() 				   { return this.marge ; }  	 // Getter de la marge


	/* Ajout d'une machine et son temps d'exécution à la HashMap */
	public void addMachine(Integer machine, Integer processTime) { this.hash.put(machine, processTime) ; }


	/* Méthode toString */
	public String toString() { return idOperation + " " + soonerDate + "/" + laterDate + "/" + marge ; }

}
