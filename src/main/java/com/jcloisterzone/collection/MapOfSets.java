package com.jcloisterzone.collection;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Sets;

public class MapOfSets<K, I> extends HashMap<K, Set<I>> {

	private static final long serialVersionUID = 8934598756570219499L;

	public static <K, I> MapOfSets<K, I> newMapOfSets() {
		return new MapOfSets<K, I>();
	}

	public boolean addElement(K key, I item) {
		Set<I> set = get(key);
		if (set == null) {
			set = Sets.newHashSet();
			put(key, set);
		}
		return set.add(item);
	}

	public boolean removeElement(K key, I item) {
		Set<I> set = get(key);
		if (set == null) {
			return false;
		}
		boolean result =  set.remove(item);
		if (set.isEmpty()) {
			remove(key);
		}
		return result;
	}

//	public MapOfSets<K, I> copyForKeys(K... keys) {
//		List<K> copyKeys = Arrays.asList(keys);
//		MapOfSets<K, I> copy = newMapOfSets();
//		for(Map.Entry<K, Set<I>> entry : entrySet()) {
//			if (copyKeys.contains(entry.getKey())) {
//				copy.put(entry.getKey(), entry.getValue());
//			}
//		}
//		return copy;
//	}


}
