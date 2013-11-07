package ch.ethz.pa;
import java.util.HashMap;
import java.util.Map;

public class IntervalPerVar {
	public IntervalPerVar() {
		values = new HashMap<String, Interval>();
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Map.Entry<String, Interval> entry : values.entrySet()) {
			if (b.length() != 0) b.append(", ");
			b.append(entry.getKey());
			b.append("=");
			b.append(entry.getValue().toString());
		}		
		return b.toString();
	}	
	
	// This does deep copy of values as opposed to shallow copy, but feel free to optimize.
	public void copyFrom(IntervalPerVar other) {
		values.clear();
		for (Map.Entry<String, Interval> entry : other.values.entrySet()) {
			Interval n = new Interval(0);
			n.copyFrom(entry.getValue());
			values.put(entry.getKey(), n);
		}
	}
	
	void putIntervalForVar(String var, Interval i) {
		values.put(var, i);
	}
	
	Interval getIntervalForVar(String var) {
		return values.get(var);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntervalPerVar)) return false;
		return ((IntervalPerVar)o).values.equals(values);
	}
	
	private HashMap<String, Interval> values;
}
