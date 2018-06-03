import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Map.Entry ;
import java.util.Random ;
import java.util.Set ;
import java.util.concurrent.ThreadLocalRandom ;
import javax.swing.JFrame ;
import org.jgraph.JGraph ;
import org.jgrapht.Graph ;
import org.jgrapht.alg.CycleDetector ;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.MatchingImpl ;
import org.jgrapht.ext.JGraphModelAdapter ;
import org.jgrapht.graph.SimpleDirectedWeightedGraph ;

public class Algo {

	private HashMap<Integer, Operation> operationMap ; // HashMap d'opérations
	
	/* Constructeur */
	public Algo() {

		super() ;
		
		this.operationMap = new HashMap<Integer, Operation>() ; // Initialisation à une HashMap vide
	}
		

	/* Fonction d'ajout d'une opération à la HashMap */
	public void addOperation(Operation op, Integer i) { this.operationMap.put(i, op) ; }
	

	/* Getter de la HashMap */
	public HashMap<Integer, Operation> getOperationMap() { return operationMap ; }


	/* Reinitialisation d'une opération : remise à -1 de sa SD et LD */
	public void resetOp(Operation op) {
		op.setLaterDate(-1) ;
		op.setSoonerDate(-1) ;	
	}


	/* Calcul de la date de début au plus tôt d'une opération op du graphe g */
	public double calculSoonerDate(Operation op, SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g) {
		
		/* Initialisation des variables à -1 */
		double max = -1 ;
		double date = -1 ;

		/* Boucle sur tous les arcs entrants de cet état (opération) */
		for (MyWeightedEdge e : g.incomingEdgesOf(op)) {

			if (g.getEdgeSource(e).getSoonerDate() == -1) { 	 // Si la date de l'opération origine n'a pas encore été calculé
				date = calculSoonerDate(g.getEdgeSource(e), g) ; // Appel récursif sur cette opération origine
			}
			
			if (g.getEdgeSource(e).getSoonerDate() + g.getEdgeWeight(e) > max) { // Si la somme entre la SD de l'opération d'origine et le poid de l'arc est supérieure à celui en mémoire
				max = g.getEdgeSource(e).getSoonerDate() + g.getEdgeWeight(e) ;  // Mise à jour de la durée en mémoire
			}
		}

		op.setSoonerDate(max) ; // Mise à jour de la date de début au plus tôt

		return max ; // Retour de la date de début au plus tôt de l'opération
	}
	
	
	public double calculLaterDate(Operation op, SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g) {
		
		/* Initialisation des variables à -1 */
		double min = 100000000;
		double date = 0;
		
		/* Boucle sur tous les arcs sortants de cet état (opération) */
		for (MyWeightedEdge e : g.outgoingEdgesOf(op)) { 
					
			if (g.getEdgeTarget(e).getLaterDate() == -1) {		// Si la date de l'opération destination n'a pas encore été calculé
				date = calculLaterDate(g.getEdgeTarget(e), g) ; // Appel récursif sur cette opération destination
			}
					
			if (g.getEdgeTarget(e).getLaterDate() - g.getEdgeWeight(e) < min) { // Si la somme entre la LD de l'opération de destination et le poid de l'arc est inférieure à celui en mémoire
				min = g.getEdgeTarget(e).getLaterDate() - g.getEdgeWeight(e) ;  // Mise à jour de la durée en mémoire
			}
		}

		op.setLaterDate(min) ; // Mise à jour de la date de début au plus tard
				
		return min ; // Retour de la date de début au plus tard de l'opération
	}
		

	/* Recherche de la première solution valide */
	public  HashMap<Integer, Machine> findSolution(SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> graph, int machineNumber, Operation firstOp) {

		SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g = graphCopy(graph) ; // Copie du graphe initial

		HashMap<Integer, Machine> machineMap = new HashMap<Integer, Machine>() ; // Structure de solution : HashMap de machines
		  
		/* Convertion des arcs du graphe en liste */  
		Set<MyWeightedEdge> setEdge=g.edgeSet() ;
		MyWeightedEdge[] array = setEdge.toArray(new MyWeightedEdge[setEdge.size()]) ;
		Set<Integer> values = null ;
			 
        CycleDetector<Operation, MyWeightedEdge> cycleDetector = new CycleDetector<Operation, MyWeightedEdge>(g) ; // Détection de cycles sur le graphe (pour la validité)
	 
		HashMap<Integer, Operation> opPrec = new HashMap<Integer, Operation>() ; // HashMap d'opérations
			 
		/* Boucle sur les machines (donné en entrée) */
		for (int i = 1 ; i <= machineNumber ; i++) {
			machineMap.put(i, new Machine(i)) ; // Ajout dans la HashMap solution
			machineMap.get(i).addOp(firstOp) ;  // Ajout de l'état de début dans les opérations de chaque machine
			opPrec.put(i, firstOp) ; 			// Ajout de l'état de début dans la HashMap d'opérations
		}

		/* Boucle sur tous les arcs du graphe */
		for (MyWeightedEdge e : array) {
			
			if (g.getEdgeSource(e).getIdOperation().equals("D")) { // Si l'arc part de l'état de départ
				g.setEdgeWeight(e, 0.0) ; // Poids de l'arc à 0
			}
			else {
				boolean pathFound = false ; // Boolean faux jusqu'à trouver un chemin valide
				
				/* Pour chaque machine possible pour l'opération */
				for (Integer k : g.getEdgeSource(e).getHash().keySet()) {
						
					if (!pathFound) { // Si le chemin n'a pas été trouvé

						if (opPrec.get(k).getIdOperation().equals("D")) { // Si l'opération précédente sur cette machine est l'état de départ
							machineMap.get(k).addOp(g.getEdgeSource(e)) ; 		 // Ajout de l'opération à la suite de la liste 
							opPrec.put(k,g.getEdgeSource(e)) ; 			  		 // Sauvegarde de l'opération
							pathFound = true ; 							  		 // Chemin trouvé
							g.setEdgeWeight(e, opPrec.get(k).getHash().get(k)) ; // Poids de l'arc : temps d'exécution pour cette machine
						}
						else {
							g.setEdgeWeight(e, opPrec.get(k).getHash().get(k)) ; 			   // Poids de l'arc : temps d'exécution pour cette machine
				            MyWeightedEdge e1 = g.addEdge(opPrec.get(k), g.getEdgeSource(e)) ; // Ajout d'un arc depuis l'opération précédente sur cette machine
				                
				            try { g.setEdgeWeight(e1, opPrec.get(k).getHash().get(k)) ; } // Poids de l'arc : temps d'exécution pour cette machine
				            catch (Exception a) {}
								
							if (cycleDetector.detectCycles()) { // Si cycle (solution non valide)
								g.removeEdge(e1) ; 								  // Suppression de l'arc crée
								machineMap.get(k).removeOp(g.getEdgeSource(e1)) ; // Suppression de l'opération dans la liste de la machine
							}
							else {
								g.setEdgeWeight(e, g.getEdgeSource(e).getHash().get(k)) ; // Poids de l'arc : temps d'exécution pour cette machine
								opPrec.put(k,g.getEdgeSource(e)) ; 						  // Sauvegarde de l'opération
								machineMap.get(k).addOp(g.getEdgeSource(e)) ; 			  // Ajout de l'opération à la suite de la liste
								pathFound = true ; 										  // Chemin trouvé
							}
						}
					}		 
				}
			}	    		 
		}

		return machineMap ; // Renvoie de la solution
	}
	

	/* Copie de graphe */
	public SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> graphCopy(SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g) {
		
		SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> copy = new SimpleDirectedWeightedGraph<Operation, MyWeightedEdge>(MyWeightedEdge.class) ; // Graphe de copie
		
		for (MyWeightedEdge e : g.edgeSet()) {
			Operation opSource = g.getEdgeSource(e) ;
			Operation opTarget = g.getEdgeTarget(e) ;
			resetOp(opSource) ;
			resetOp(opTarget) ;
			copy.addVertex(opSource) ;
			copy.addVertex(opTarget) ;
			MyWeightedEdge ec = copy.addEdge(opSource, opTarget) ;
			copy.setEdgeWeight(ec, g.getEdgeWeight(e)) ;
		}
		
		return copy ; // Revoie de la copie
	}


	/* Évaluation d'une solution */
	public double buildSolution(HashMap<Integer, Machine> machineMap, SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> graph, Operation firstOp, Operation lastOp, boolean draw) {
		
		SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g = graphCopy(graph) ; // Copie du graphe initial
	
        CycleDetector<Operation, MyWeightedEdge> cycleDetector = new CycleDetector<Operation,MyWeightedEdge>(g) ; // Détecteur de cycles
        
        /* Parcours de la solution (machines) */
       	for (Entry<Integer, Machine> m : machineMap.entrySet()) {
	    	Integer key = m.getKey() ; 	   // Récupération de l'identifiant de la machine
	    	Machine value = m.getValue() ; // Récupération de la machine
			Operation opPrec = firstOp ;   // Sauvegarde de la première opération
	    	
	    	/* Parcours de la liste ordonnée des opérations de cette machine */
	    	for (Operation op : value.getOpList()) {
	    		
	    		if (op.getIdOperation().equals("D")) {} // On ne fait rien pour l'opération de départ
	    		else {
	    	
	    			try {
	    			 
		            	if (opPrec.getIdOperation().equals("D")) { // Si l'opération précédente sur la liste de la machine est l'état de départ
		            	 
		            		if (value.getOpList().size() == 2) { // Si la liste contient uniquement 2 opérations (dont celle de départ)
		            			MyWeightedEdge e1 = g.getEdge(op, lastOp) ;
				            	g.setEdgeWeight(e1, op.getHash().get(key)) ; // On met à jour le poids de l'arc entre l'opération courante et l'état de fin
		            	 	}
		            	}
		            	else {

		            		if (g.containsEdge(opPrec, op)) { // Si l'arc entre l'opération précédente et la courante existe déjà
		            			MyWeightedEdge e1 = g.getEdge(opPrec, op) ;
				            	g.setEdgeWeight(e1, opPrec.getHash().get(key)) ; // Mise à jour du poids de l'arc
		            	 	}
		            	 	else { // Sinon
		            		
		            			if (value.getOpList().get(value.getOpList().size()-1).getIdOperation().equals(op.getIdOperation())) { // S'il s'agit de la dernière opération pour cette machine
		            				g.setEdgeWeight((MyWeightedEdge)g.outgoingEdgesOf(op).toArray()[0], op.getHash().get(key)) ; 	  // Mise à jour du poids de tous ses arcs sortants
		            		 	}

		            			MyWeightedEdge e1 = g.addEdge(opPrec, op) ; 														 // Création de l'arc entre l'opération précédente sur cette machine et l'opération courante
		            			g.setEdgeWeight((MyWeightedEdge)g.outgoingEdgesOf(opPrec).toArray()[0], opPrec.getHash().get(key)) ; // Mise à jour des poids des arcs
		            			g.setEdgeWeight(e1, opPrec.getHash().get(key)) ;
		            		}
		             	}
	    			} catch (Exception e) {}
	    		 
	    			if(cycleDetector.detectCycles()) { return -1 ; } // Si l'on détecte un cycle : solution non valide, renvoie -1
	    			else { opPrec = op ; } 							 // Sauvegarde de l'opération précédente
	    		}
	    	}
	    }

		firstOp.setSoonerDate(0) ; 				   // Définition de la SD de l'état de départ à 0
        double max = calculSoonerDate(lastOp, g) ; // Calcul de la SD de l'état final (temps d'exécution) (va calculer les SD de toutes les opérations)
        lastOp.setLaterDate(max) ; 				   // Définition de la LD de l'état final à sa SD
        double min = calculLaterDate(firstOp, g) ; // Calcul de la LD de l'état initial (va calculer les SD de toutes les opérations et permettre de connaitre les opérations critiques grace à la marge)
		
		/* Dessin du graphe si draw = true */
        if (draw) {
        	JFrame frame = new JFrame() ;
    		frame.setSize(400, 400) ;
    		JGraph jgraph = new JGraph(new JGraphModelAdapter(g)) ;
    		frame.getContentPane().add(jgraph) ;
    		frame.setVisible(true) ;
        }
		 
		return max ; // Retour tu temps total d'exécution de la solution	 
	}
	
	
	/* Amélioration d'une solution par Hill Climbing */
	public HashMap<Integer, Machine> hillClimbing(SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> graph, Operation finalOp, Operation firstOp, HashMap<Integer, Machine> machineMap) {
	
		/* Clones de la solution */
		HashMap<Integer, Machine> aux = (HashMap<Integer, Machine>) machineMap.clone() ;
		HashMap<Integer, Machine> sauv = (HashMap<Integer, Machine>) machineMap.clone() ;
		
		SimpleDirectedWeightedGraph<Operation, MyWeightedEdge> g = graphCopy(graph) ; // Copie du graphe initial
		    
		double time = buildSolution(aux, g, firstOp, finalOp, false) ; // Récupération du temps d'exécution de la solution initiale à améliorer via son évaluation
		System.out.println("Initial time : " + time) ; // Affichage du temps initial
		
		int notfound = 0 ;

		/* Amélioration par changement de machines sur chemin critique */
		while (notfound < 10) outerloop : { // Arrêt au bout de 10 nouvelles solutions n'améliorant pas le temps d'exécution (choix à adapter à la taille du JobShop)
			aux = (HashMap<Integer, Machine>) sauv.clone() ; // Sauvegarde de la solution en cours
			
			/* Parcours de la solution en cours */
			for (Entry<Integer, Machine> machine : aux.entrySet()) {
			 
			 	/* Récupération de la machine en cours et de son identifiant */
	    		Machine m = machine.getValue() ;
	    		Integer key = machine.getKey() ;

    	 		/* Boucle sur la liste des opérations de cette machine */
	    		for (Operation op : new ArrayList<Operation>(m.getOpList())) {

	    			if (op.getMarge() == 0.0) { // Si on est sur une opération critique
 
	    				if (!op.getIdOperation().equals("D") && op.getHash().size() > 1) { // S'il ne s'agit pas de l'état de départ et qu'il y a plus d'une machine possible pour cette opération
	  
	  						/* Parcours des machines possibles */
	    					for (Integer k : op.getHash().keySet()) {
    		    	 
	    		    	 		if (k != key) { // Vérification qu'on ne traite pas la même machine que précédemment
									sauv = (HashMap<Integer, Machine>) aux.clone() ; // Sauvegarde de la solution
	    		    		 
	    		    				sauv.get(key).removeOp(op) ; // Suppression de l'opération de la liste d'opérations de la machine précédente
	    		    				sauv.get(k).addOp(op) ; 	 // Ajout de l'opération de la liste d'opérations de la nouvelle machine
	    		    		
	    		    				double test = buildSolution(sauv, graph, firstOp, finalOp, false) ; // Évaluation de cette nouvelle solution
	    		    		 
	    		    		 		if (test != -1 && test < time) { // Si la solution et valide et améliore le temps d'exécution
	    		    			 		time = test ; 	  // Mise à jour du temps de référence
	    		    					break outerloop ; // On continue avec cette nouvelle solution
	    		    		 		}
	    		    		 		else {
	    		    		 			/* Remise à état */
	    		    			 		sauv.get(k).removeOp(op) ;
		    		    				sauv.get(key).addOp(op) ;
	    		    					notfound++ ; // Incrémentation du nombre de solution moins bonnes
	    		    		 		}
	    		    	 		}
	    				  	}
	    				}
	    		 	}
	    		}
			}
		}
		
		notfound = 0 ;

		/* Amélioration par changement d'ordre d'opérations pour chaque machine */
		while (notfound < 10) outerloop2 : {
			aux = (HashMap<Integer, Machine>) sauv.clone() ; // Sauvegarde de la solution en cours
		
			/* Parcours de la solution en cours */
			for (Entry<Integer, Machine> machine : aux.entrySet()) {

				/* Récupération de la machine en cours et de son identifiant */
				Machine m = machine.getValue() ;
				Integer key = machine.getKey() ;
	    		
	    		int index = 0 ;
    	 
    	 		/* Parcours de la liste ordonnée des opérations de cette machine */
	    		for (Operation op : new ArrayList<Operation>(m.getOpList())) {

	    			if (op.getMarge() == 0.0) { // Si opération critique
 
	    				if (!op.getIdOperation().equals("D") && op.getHash().size() > 1) { // S'il ne s'agit pas de l'état de départ et qu'il y a plus d'une opération sur cette machine
	    					sauv = (HashMap<Integer, Machine>) aux.clone() ; // Sauvegarde de la solution en cours
	    		    		 
	    		    		/* Génération d'un numéro aléatoire entre 0 et la taille de la liste -1 */
	    		    		Random random = new Random() ;
	    		    		int randomNumber = random.nextInt(m.getOpList().size() - 1) ;
	    		    		
	    		    		sauv.get(key).removeOp(op) ; 					  // Suppression de l'opération de la liste
	    		    		sauv.get(key).getOpList().add(randomNumber, op) ; // Ajout de l'opération à la liste à un indice aléatoire
	    		    		
	    		    		double test = buildSolution(sauv, graph, firstOp, finalOp, false) ; // Évaluation de cette nouvelle solution
	    		    		 
	    		    		if (test != -1 && test < time) { // Si la solution et valide et améliore le temps d'exécution
	    		    			time = test ; 	   // Mise à jour du temps de référence
	    		    			break outerloop2 ; // On continue avec cette nouvelle solution
    		    			}
    		    			else {
    		    				/* Remise à état */
	    		    			sauv.get(key).removeOp(op) ;
		    		    		sauv.get(key).getOpList().add(index, op) ;
		    		    		notfound++ ; // Incrémentation du nombre de solution moins bonnes
	    		    		}
	    		    	} 
	    			}
    			 
    			 	index++ ; // Incrémentation de l'indice
	    		}
	    	}
	    }
	
		time = buildSolution(aux, g, firstOp, finalOp, true) ; // Évaluation de la nouvelle solution et dessin du graphe
		
		/* Affichage de la solution (opérations par machine) */
		for (Entry<Integer, Machine> m : aux.entrySet()) {
	    	Integer key = m.getKey() ;
	    	Machine value = m.getValue() ;
	    	 
	    	System.out.println("OP machine " + value.getIdMachine()) ;
	    	 
	    	for (Operation op :value.getOpList()) {
	    		
	    		if (op.getIdOperation().equals("D")) {}
	    		else {
	    			System.out.print(op.getIdOperation() + " : " + value.getIdMachine() + " -> ") ;
	    		}
	    	}
	    	System.out.println("") ;
	    }
		
		System.out.println("Time found : " + time) ;
		
		return aux ; // Retour de la solution trouvée
	}
	
}