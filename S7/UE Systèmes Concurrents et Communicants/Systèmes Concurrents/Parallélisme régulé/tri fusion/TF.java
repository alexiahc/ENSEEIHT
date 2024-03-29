//v0.2.2 15/11/21 (PM) (0.2..2 : modification (mineure) des parties à compléter)

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.RecursiveTask;

import java.util.concurrent.ForkJoinPool;

class TriLocal  implements Callable<int[]> {
// pool fixe
    private int début;
    private int fin;
    private int[] tableau;
    private int[] résultat;

    TriLocal(int[] t, int d, int f) {
        début = d;
        fin = f;
        tableau = t;
    }

    @Override
    public	int[] call() {
        return TF.traiterTronçon(tableau, début, fin) ;
    }
}

class TraiterFragment extends RecursiveTask<int[]> {
// pool fork-join
    private int début;
    private int fin;
    private int[] tableau;
    int[] resTri=null;

    private int max = 0;

    TraiterFragment(int[] t, int d, int f) {
        début = d;
        fin = f;
        tableau = t;
    }

    @Override
    protected int[] compute() {
        int taille;
        //si la tâche est trop grosse, on la décompose en 2
// ********* A compléter
        taille = fin - début;
        if (taille > TF.seuil) {
        	TraiterFragment tf1 = new TraiterFragment(tableau, début, (début+fin)/2-1);
        	TraiterFragment tf2 = new TraiterFragment(tableau, (début+fin)/2, fin);
        	
        	tf1.fork();
        	tf2.fork();
        	
        	resTri = TF.fusion(tf1.join(),tf2.join());
        	return resTri;
        } else {
        	resTri = TF.traiterTronçon(tableau, début, fin);
        	return resTri;
        }
    	
    }
}

public class TF {
    static int seuil;

    static int[] fusion(int[] t1, int[]t2) {
        // fusionne les tableaux triés t1 et t2 en un seul tableau trié (résultat)
        int [] résultat = new int[t1.length+t2.length];
        int i1=0;
        int i2=0;
        int iR=0;

        while ((iR<résultat.length) && (i1<t1.length) && (i2<t2.length)) {
            if (t1[i1]<t2[i2]) {
                résultat[iR]=t1[i1];
                i1++;
            } else {
                résultat[iR]=t2[i2];
                i2++;
            }
            iR++;
        }
        while (i1<t1.length) {
            résultat[iR]=t1[i1];
            i1++;
            iR++;
        }
        while (i2<t2.length) {
            résultat[iR]=t2[i2];
            i2++;
            iR++;
        }
        return résultat;
    }

    static int[] traiterTronçon(int[] t, int début, int fin) {
        int taille;
        int[] resTri;
        //si le fragment est trop gros, on le décompose en 2
        if((fin-début+1) > TF.seuil) {
            taille = (fin-début+1)/2;
            return TF.fusion(traiterTronçon(t, début, début+taille-1),
            										traiterTronçon(t, début+taille, fin));
        } else { //traitement direct : quicksort
            resTri = Arrays.copyOfRange(t, début,fin+1);
            Arrays.sort(resTri);
            return resTri;
        }
    }

//--------- Mono
    static int[] TFMono(int[] t) {
        return traiterTronçon(t, 0, t.length-1);
    }

//-------- Pool Fixe
    static int[] TFPool(ExecutorService xs, int[] t, int nbT)
    throws InterruptedException, ExecutionException {
        
        List<Future<int[]>> résultats = new ArrayList<Future<int[]>>(nbT);
        List<int[]> resTri = new LinkedList<int[]>(); //recueille et fusionne les résultats
        int grain = Math.max(1,t.length/nbT); 	/*taille d'une recherche locale 
        										 * = taille du tableau/nombre de tâches soumises
        										 * (ou 1 dans le cas (aberrant) où il y a plus
        										 * de tâches que d'éléments du tableau) */
// ********* A compléter (initialisation des variables)
        int d = 0;							//indice de départ d'une recherche locale
        int f = nbT;  						//indice de fin d'une recherche locale

        //soumettre les tâches
// ********* A compléter
        ExecutorService poule = Executors.newFixedThreadPool(grain);
        for (int i = 0 ; i < t.length-1 ; i += f) {
        	résultats.add( poule.submit(new TriLocal(t, d + i, i + d + f-1)) );
        }
        poule.shutdown();
        
        //récupérer et fusionner les résultats.
// ********* A compléter
// besoin d'une fonction pour concatener les tableaux de résultats ? 
        int[] list_res ;
        for (Future<int[]> k : résultats) {
        	list_res = k.get();
        	resTri.add(list_res);
        }
        
        int[] resListTri;
        resListTri = Arrays.copyOf(t, t.length);
        int j = 0;
        for (int[] tab :resTri) {
        	for (int i = 0 ; i < tab.length ; i++) {
        		resListTri[j] = tab[i];
        		j++;
        	}
        }
        
		return resListTri; // *********** A corriger
    }
    
//-------- Pool ForkJoin
    static int[] TFForkJoin(ForkJoinPool f, int[] t) {
        TraiterFragment tout = new TraiterFragment(t,0, t.length-1);
        int[] resTri = f.invoke(tout); // ********* A corriger
        return resTri;
    }

//-------- main
    public static void main(String[] args)
    throws InterruptedException, ExecutionException, IOException, FileNotFoundException {

        int nbOuvriersPool=0; // nb ouvriers du pool fixe. Bonne valeur : nb de processeurs disponibles
        int nbEssais=0;
        int nbTâches=0;
        int tailleSeuil=0; //nombre d'éléments du tableau en dessous duquel on effectue un tri directement
        String chemin="";
        int[] tableau;
        long départ, fin;
        int [] resTri;

        long[] tempsMono, tempsPool,tempsForkJoin;

        if (args.length == 5) { //analyse des paramètres
            chemin = args[0];
            try {
                nbEssais = Integer.parseInt (args[1]);
                nbTâches = Integer.parseInt (args[2]);
                tailleSeuil = Integer.parseInt (args[3]);
                nbOuvriersPool = Integer.parseInt (args[4]);
            }
            catch (NumberFormatException nfx) {
                throw new IllegalArgumentException("\nUsage : TF <fichier> <nb essais> "
                                                   + " <nb tâches (pool)> <seuil>"
                                                   + " <nb ouvriers du pool (pool)>\n"
                                                   + " * <nb tâches (pool)> = nb de fragments à traiter \n"
                                                   + " * <seuil> = taille pour tri direct \n");
            }
        }

        if ((nbEssais<1) || (nbTâches<1) || (tailleSeuil<1) || (nbOuvriersPool<1)
                || !Files.isRegularFile(Paths.get(chemin), LinkOption.NOFOLLOW_LINKS))
            throw new IllegalArgumentException("\nUsage : TF <fichier> <nb essais> "
                                               + " <nb tâches (pool)> <seuil>"
                                               + " <nb ouvriers du pool (pool)>\n"
                                               + " * <nb tâches (pool)> = nb de fragments à traiter \n"
                                               + " * <seuil> = taille pour tri direct \n");
        // l'appel est correct
        tempsMono = new long[nbEssais];
        tempsPool = new long[nbEssais];
        tempsForkJoin = new long[nbEssais];
        tableau=TableauxDisque.charger(chemin);
        TF.seuil=tailleSeuil;

        System.out.println(Runtime.getRuntime().availableProcessors()+" processeurs disponibles pour la JVM");

        //créer un pool avec un nombre fixe d'ouvriers
        ExecutorService poule = Executors.newFixedThreadPool(nbOuvriersPool);

        //créer un pool ForkJoin
        ForkJoinPool fjp = new ForkJoinPool();

        for (int i = 0; i < nbEssais; i++) {
            départ = System.nanoTime();
            resTri = TFMono(tableau);
            fin = System.nanoTime();
            tempsMono[i] = (fin - départ);
            TableauxDisque.sauver(chemin+"triéMono",resTri);
            System.out.println("Essai ["+i+"] : durée (mono) " + tempsMono[i]/1_000+"µs");
        }
        System.out.println("--------------------");

        for (int i = 0; i < nbEssais; i++) {
            départ = System.nanoTime();
            resTri = TFPool(poule, tableau, nbTâches);
            fin = System.nanoTime();
            tempsPool[i] = (fin - départ);
            TableauxDisque.sauver(chemin+"triéPF",resTri);
            System.out.println("Essai ["+i+"] : durée (PF) " + tempsPool[i]/1_000+"µs");
        }
        poule.shutdown();
        System.out.println("--------------------");

        for (int i = 0; i < nbEssais; i++) {
            départ = System.nanoTime();
            resTri = TFForkJoin(fjp,tableau);
            fin = System.nanoTime();
            tempsForkJoin[i] = (fin - départ);
            TableauxDisque.sauver(chemin+"triéFJ",resTri);
            System.out.println("Essai ["+i+"] : durée (FJ) "+tempsForkJoin[i]/1_000+"µs");
        }
        System.out.println("--------------------");
    }
}
