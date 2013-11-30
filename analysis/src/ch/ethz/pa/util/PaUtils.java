package ch.ethz.pa.util;

import java.util.Iterator;

public class PaUtils {
	public static <T> String join(Iterable<T> s, String delimiter) {
	    Iterator<T> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next().toString());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next().toString());
	    return buffer.toString();
	}
	
	public static <T> String joinRecursive(Iterable<T> s, String delimiter, String nestBegin, String nestEnd) {
	    if(s instanceof Object){
			Iterator<T> iter = s.iterator();
		    if (!iter.hasNext()) return "";
		    StringBuilder buffer = new StringBuilder();
		    T next;
		    boolean first = true;
		    while (iter.hasNext()){
			    next = iter.next();
			    if(next instanceof Iterable<?> && !(next instanceof String)){
			    	//print nested
			    	Iterator<?> nestedIterator = ((Iterable<?>)next).iterator();
			    	if(nestedIterator.hasNext()){
				    	buffer.append(nestBegin);
				    	buffer.append(joinRecursive((Iterable<?>)next, delimiter, nestBegin, nestEnd));
				    	buffer.append(nestEnd);
			    	}
			    } else {
			    	if(!first){
			    		buffer.append(delimiter);
			    	}
			    	buffer.append(iter.next().toString());
			    }
			    first = false;
		    }
		   
		    return buffer.toString();
	    } else {
	    	return "";
	    }
	}
	
}
