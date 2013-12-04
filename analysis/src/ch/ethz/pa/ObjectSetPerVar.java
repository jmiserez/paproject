package ch.ethz.pa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.tagkit.Tag;
import ch.ethz.pa.util.PaUtils;

public class ObjectSetPerVar{
	
	private HashMap<String, HashSet<Tag>> objects; //TODO replace object with Soot type

	public ObjectSetPerVar() {
		objects = new HashMap<String, HashSet<Tag>>();
	}
	
	@Override
	public String toString() {
		return PaUtils.prettyPrintIterableRecursive(objects.entrySet(), ", ", "[", "]");
	}
	
	public void copyFrom(ObjectSetPerVar other) {
		objects.clear();
		for (Map.Entry<String, HashSet<Tag>> entry : other.objects.entrySet()) {
			objects.put(entry.getKey(), new HashSet<Tag>());
			objects.get(entry.getKey()).addAll(entry.getValue()); //shallow copy of Set
		}
	}

	public static void join(ObjectSetPerVar src1, ObjectSetPerVar src2,
			ObjectSetPerVar trg) {
		for (Map.Entry<String, HashSet<Tag>> entry : src1.objects.entrySet()) {
			trg.addAllObjectSetForVar(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, HashSet<Tag>> entry : src2.objects.entrySet()) {
			trg.addAllObjectSetForVar(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Overwrites the set for var
	 * @param var
	 * @param set
	 */
	void putObjectSetForVar(String var, Set<Tag> set) {
		if(!objects.containsKey(var)){
			objects.put(var, new HashSet<Tag>());
		}
		objects.get(var).clear();
		objects.get(var).addAll(set);
	}
	
	Set<Tag> getObjectSetForVar(String var){
		HashSet<Tag> s = objects.get(var);
		if (s == null)
			return new HashSet<Tag>();
		else
			return s;
	}
	
	void addAllObjectSetForVar(String var, Set<Tag> set) {
		if(!objects.containsKey(var)){
			objects.put(var, new HashSet<Tag>());
		}
		objects.get(var).addAll(set);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ObjectSetPerVar)) return false;
		ObjectSetPerVar other = (ObjectSetPerVar) o;
		return other.objects.equals(objects);
	}
}
