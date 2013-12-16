package ch.ethz.pa.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PaUtils {
	public static <T> String prettyPrintIterable(Iterable<T> s, String delimiter) {
	    Iterator<T> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next().toString());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next().toString());
	    return buffer.toString();
	}
	
	public static <T> String prettyPrintIterableRecursive(Iterable<T> s, String delimiter, String nestBegin, String nestEnd) {
	    if(s instanceof Object){
			Iterator<T> iter = s.iterator();
		    if (!iter.hasNext()) return "";
		    StringBuilder buffer = new StringBuilder();
		    T next;
		    boolean first = true;
		    while (iter.hasNext()){
			    next = iter.next();
			    if(next instanceof Iterable<?>){
			    	//print nested
			    	Iterator<?> nestedIterator = ((Iterable<?>)next).iterator();
			    	if(nestedIterator.hasNext()){
				    	buffer.append(nestBegin);
				    	buffer.append(prettyPrintIterableRecursive((Iterable<?>)next, delimiter, nestBegin, nestEnd));
				    	buffer.append(nestEnd);
			    	}
			    } else {
			    	if(!first){
			    		buffer.append(delimiter);
			    	}
			    	buffer.append(next.toString());
			    }
			    first = false;
		    }
		   
		    return buffer.toString();
	    } else {
	    	return "";
	    }
	}
	
	/**
	 * Can copy any (nested) Map which has a default constructor
	 */
	@SuppressWarnings("unchecked")
	public static <K,V, M extends Map<K,V>> M deepCopyMap(M source){
		M result = null;
		try {
			result = (M) source.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		for(Map.Entry<K, V> entry : source.entrySet()){
			K k = entry.getKey();
			V v = entry.getValue();
			if(k instanceof Map<?,?>){
				k = (K) deepCopyMap((Map<?,?>) k);
			}
			if(v instanceof Map<?,?>){
				v = (V) deepCopyMap((Map<?,?>) v);
			}
			result.put(k, v);
		}
		return result;
	}
	
	/**
	 * Can copy any (nested) Set which has a default constructor
	 */
	@SuppressWarnings("unchecked")
	public static <E, S extends Set<E>> S deepCopySet(S source){
		S result = null;
		try {
			result = (S) source.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		for(E e : source){
			if(e instanceof Set<?>){
				e = (E) deepCopySet((Set<?>) e);
			}
			result.add(e);
		}
		return result;
	}
	
	public static BigInteger bigIntSetKthBitToOne(BigInteger b, int k){
		return b.or(new BigInteger("1").shiftLeft(k));
	}
	
	public static BigInteger bigIntSetKthBitToZero(BigInteger b, int k){
		return b.and(new BigInteger("1").shiftLeft(k).not());
	}
	
	public static BigInteger bigIntAllZeros(){
		return new BigInteger("0");
	}
	
	public static BigInteger bigIntAllOnes(){
		return new BigInteger(String.valueOf(Integer.MAX_VALUE)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());
	}
	
}
