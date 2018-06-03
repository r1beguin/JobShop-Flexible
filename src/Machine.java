import java.util.ArrayList ;

public class Machine {

	/* Attributs */

	private int idMachine ; 			  // Identifiant
	
	private ArrayList<Operation> opList ; // Liste ordonnée des opérations s'exécutant sur cette machine


	/* Constructeur */
	public Machine(int idMachine) {

		super() ;

		this.idMachine = idMachine ;

		this.opList = new ArrayList<Operation>() ; // Initialisation à une liste vide
	}


	/* Getters */

	public int getIdMachine() 				{ return idMachine ; } 	 // Getteur de l'identifiant
	
	public ArrayList<Operation> getOpList() { return this.opList ; } // Getteur de la liste d'opérations


	/* Méthodes d'ajout et de suppression d'une opération de la liste */
	
	public void addOp(Operation op)    { this.opList.add(op) ; }
	
	public void removeOp(Operation op) { this.opList.remove(op) ; }

}
