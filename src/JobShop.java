import java.io.File ;
import org.jgrapht.alg.CycleDetector ;
import org.jgrapht.graph.* ;
import java.io.FileInputStream;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.util.HashMap ;
import java.util.Scanner ;


public class JobShop {
	
	/* Fonction main lancée à l'exécution */
	public static void main(String[] args) {

		Algo algo = new Algo() ;// Utilisation des méthodes définies dans la classe Algo
    	
    	SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g = new SimpleDirectedWeightedGraph<Operation, MyWeightedEdge>(MyWeightedEdge.class) ; // Graphe à remplir
	    
	    FileInputStream fis = null ;

	    try {
	        
	        try { fis = new FileInputStream(new File("src/Mk01.fjs")) ; } // Fichier source du JobShop
			catch (FileNotFoundException e) { e.printStackTrace() ; } 	  // Si le fichier n'existe pas    

			Scanner scanner = new Scanner(fis) ; // Début de scann du fichier source     	         

			/* Initialisation du nombre de jobs et de machines */
	        int jobNumber = 0 ;
	        jobNumber = scanner.nextInt() ; 	//Lecture du nombre de job
	        int machineNumber = 0 ;
	        machineNumber = scanner.nextInt() ; //Lecture du nombre de machine et création de la hashmap correspondante
	        
	        /* Nombre moyen de machines par opérations (inutile pour la suite) */
	        double average = 0 ;
	        average = scanner.nextDouble() ; //Average number of machine used by operation

	    	/* Compteurs */
	        int currentJob = 1 ;  // Numéro du Job courrant
	        int currentOp = 1 ;   // Numéro de l'opération courante dans le Job courant
	        int currentOpId = 0 ; // Numéro global de l'opération courante

	        /* Variables */
			int operationNumber = 0 ; 	  // Nombre d'opérations dans le Job
	        int machinePerOperation = 1 ; // Nombre de machines possibles pour l'opération
	        int machineId = 0 ; 		  // Identifiant de la machine
	        int processTime = 0 ; 		  // Temps d'exécution
	        
	        /* État de début */
	        Operation firstOp = new Operation("D") ;  // Création
	        algo.addOperation(firstOp, currentOpId) ; // Ajout à la HashMap d'opérations
        	g.addVertex(firstOp) ; 					  // Ajout au graphe
        	
        	currentOpId++ ; // Incrémentation du numéro global d'opération
        	
        	/* État de fin */
        	Operation lastOp = new Operation("F") ;   // Création
        	algo.addOperation(firstOp, currentOpId) ; // Ajout à la HashMap d'opérations
        	g.addVertex(lastOp) ; 					  // Ajout au graphe
        	
        	currentOpId++ ; // Incrémentation du numéro global d'opération
              
	        /* Boucle sur l'ensemble du document */
	        while (scanner.hasNext()) {
	        	operationNumber = scanner.nextInt() ; // Nombre d'opérations dans le Job courant 

				Operation aux = firstOp ; // Sauvegarde de l'opération de début

	        	/* Boucle sur les opérations de ce Job */
	        	for (int i = 1 ; i <= operationNumber ; i++) {
	        	 	machinePerOperation = scanner.nextInt() ; // Nombre de machines possibles pour cette opération
	        	 				        	 		
	        	 	Operation opCurrent = new Operation("O" + currentJob + "-" + currentOp) ;
	        	 	g.addVertex(opCurrent) ;

	        	 	if (currentOp == 1) { // Cas de la première opération du Job
						MyWeightedEdge e = g.addEdge(firstOp, opCurrent) ; // Ajout d'un arc entre l'état de début et cette opération
	            		g.setEdgeWeight(e, 0) ; 						   // Poids 0 sur cet arc
	            	}
	            	else {
	        	 		MyWeightedEdge e = g.addEdge(aux, opCurrent) ; // Ajout d'un arc entre l'opération précédente et la courante
		                g.setEdgeWeight(e, 0) ; 					   // Poids 0 sur cet arc

		                if (currentOp == operationNumber) { // Cas de la dernière opération du Job
		                	MyWeightedEdge e2 = g.addEdge(opCurrent, lastOp) ; // Ajout d'un arc entre cette opération et l'état de fin
			            	g.setEdgeWeight(e2, 0) ; 						   // Poids 0 sur cet arc
			            }
		            }
	        	 		
	        	 	/* Boucle pour chaque machine possible pour cette opération */
	        	 	for (int j = 0 ; j < machinePerOperation ; j++) {
	        	 		machineId = scanner.nextInt() ; 			   // Identifiant de la machine
	        	 		processTime = scanner.nextInt() ; 			   // Temps d'exécution correspondant
	        	 		opCurrent.addMachine(machineId, processTime) ; // Ajout à la HashMap de l'opération
	        	 	}
 		
	        	 	currentOp++ ; // Incrémentation du numéro d'opération dans le Job

	        	 	aux = opCurrent ; // Sauvegarde de l'opération précédente
	        	}

	        	currentOp = 1 ; 	 // Retour à 1 avant chaque nouveau Job
	        	currentJob++ ; 		 // Incrémentation du numéro de Job courant
	        	scanner.nextLine() ; // Nouvelle ligne
	        }
	          
            CycleDetector<Operation, MyWeightedEdge> cycleDetector = new CycleDetector<Operation, MyWeightedEdge>(g) ; // Détection de cycle sur le graphe
            
            scanner.close() ; // Fermeture du scanner

        	HashMap<Integer, Machine> machineMap = algo.findSolution(g, machineNumber, firstOp) ; // Recherche de la solution initiale

            algo.hillClimbing(g, lastOp, firstOp, machineMap) ; // Amélioration de la solution initiale trouvée
             
	    } finally {
	        
	        try { fis.close() ; } // Fermeture du fichier
			catch (IOException e) { e.printStackTrace() ; }
	    }
	}

}