package ec.alps.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ascending sort
 * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
 * 
 * http://www.mkyong.com/java/how-to-sort-a-map-in-java/
 * 
 */
public class MapUtil {

	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
				} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}


	//Java 7 Version
	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByValue7( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<>( map.entrySet() );
				Collections.sort( list, new Comparator<Map.Entry<K, V>>()
						{
					@Override
					public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
					{
						return (o1.getValue()).compareTo( o2.getValue() );
					}
						} );

				Map<K, V> result = new LinkedHashMap<>();
				for (Map.Entry<K, V> entry : list)
				{
					result.put( entry.getKey(), entry.getValue() );
				}
				return result;
	}


	/*Java 8 Version
	public static <K, V extends Comparable<? super V>> Map<K, V> 
	    sortByValue8( Map<K, V> map )
	{
	     Map<K,V> result = new LinkedHashMap<>();
	     Stream <Entry<K,V>> st = map.entrySet().stream();

	     st.sorted(Comparator.comparing(e-> e.getValue()))
	          .forEach(e->result.put(e.getKey(),e.getValue()));

	     return result;
	}
	 */
	
	/**
	 * sorted in ascending order
	 * @param unsortMap
	 * @return
	 */
	public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) 
		{
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
 
	public static void printMap(Map<String, Double> map) 
	{
		for (Map.Entry<String, Double> entry : map.entrySet()) 
		{
			System.out.println("[Key] : " + entry.getKey() 
                                      + " [Value] : " + entry.getValue());
		}
	}
	
	public void test()
	{
		Map<String, Double> unsortMap = new HashMap<String, Double>();
		unsortMap.put("z", 10.0);
		unsortMap.put("b", 5.0);
		unsortMap.put("a", 6.0);
		unsortMap.put("c", 8.0);
		unsortMap.put("d", 1.0);
		unsortMap.put("e", 9.0);
		unsortMap.put("y", 8.0);
		unsortMap.put("n", 99.0);
		unsortMap.put("j", 50.0);
		unsortMap.put("m", 2.0);
		unsortMap.put("f", 9.0);
 
		System.out.println("Unsort Map......");
		printMap(unsortMap);
 
		System.out.println("\nSorted Map......");
		Map<String, Double> sortedMap = sortByComparator(unsortMap);
		printMap(sortedMap);
		
		System.out.println("Unsort Map......");
		printMap(unsortMap);
	}
	


}
