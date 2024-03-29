/**
 * StatistiquesTest, classe de test de Statistiques.
 *
 * @author	Xavier Crégut &lt;Prenom.Nom@enseeiht.fr&gt;
 */

package fr.n7.gls.test;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;

public class StatistiquesTest {
	
	List<Integer> l;
	
	@Before
	public void initialise() {
		l = new ArrayList<>();
	}
	

	@Test
	public void testStatistiquesNominal() {
		Collections.addAll(l, 1, 3, 11, 5, 7);
		Statistiques.Resultat<Integer> r = new Statistiques().statistiques(l);
		assertEquals(Integer.valueOf(1), r.min);
		assertEquals(Integer.valueOf(11), r.max);
		assertEquals(1, r.nbMin);
		assertEquals(1, r.nbMax);
		
	}
	
	@Test 
	public void testVide() {
		Statistiques.Resultat<Integer> r = new Statistiques().statistiques(l);
		assertEquals(null, r.min);
		assertEquals(null, r.max);
		assertEquals(0, r.nbMin);
		assertEquals(0, r.nbMax);
	}
	
	@Test
	public void testmin() {
		Collections.addAll(l, 4, 3, 1, 1, 4);
		Statistiques.Resultat<Integer> r = new Statistiques().statistiques(l);
		assertEquals(Integer.valueOf(1), r.min);
		assertEquals(Integer.valueOf(4), r.max);
		assertEquals(2, r.nbMin);
		assertEquals(2, r.nbMax);
		
		
	}
	

}
