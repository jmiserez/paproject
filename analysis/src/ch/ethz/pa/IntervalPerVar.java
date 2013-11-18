package ch.ethz.pa;
import java.util.HashMap;
import java.util.Map;

import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JimpleLocal;

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
			Interval n = new Interval();
			n.copyFrom(entry.getValue());
			values.put(entry.getKey(), n);
		}
	}
	
	public static void join(IntervalPerVar src1, IntervalPerVar src2, IntervalPerVar trg) {
		for (Map.Entry<String, Interval> entry : src1.values.entrySet()) {
			trg.putIntervalForVar(entry.getKey(), entry.getValue().join(src2.getIntervalForVar(entry.getKey())));
		}
	}
	
	void putIntervalForVar(String var, Interval interval) {
		values.put(var, interval);
	}
	
	Interval getIntervalForVar(String var) {
		Interval i = values.get(var);
		if (i == null)
			return Interval.BOT.copy();
		else
			return i;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntervalPerVar)) return false;
		return ((IntervalPerVar)o).values.equals(values);
	}
	
	private HashMap<String, Interval> values;

}
