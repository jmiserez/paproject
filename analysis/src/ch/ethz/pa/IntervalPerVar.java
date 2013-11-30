package ch.ethz.pa;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.pa.domain.AbstractDomain;
import ch.ethz.pa.domain.Domain;
import ch.ethz.pa.util.PaUtils;

public class IntervalPerVar {
	
	private HashMap<String, AbstractDomain> values;
	private HashMap<String, HashMap<Integer, Integer>> adjustValueCount; //TODO: add this to the other methods
	private HashMap<String, HashMap<Integer, Integer>> readSensorCount; //TODO: add this to the other methods
	
	public IntervalPerVar() {
		values = new HashMap<String, AbstractDomain>();
		adjustValueCount = new HashMap<String, HashMap<Integer, Integer>>();
		readSensorCount = new HashMap<String, HashMap<Integer, Integer>>();
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		String delim = ", ";
		b.append("values = [");
		b.append(PaUtils.join(values.entrySet(), delim));
		b.append("], adjustValueCount = [");
		b.append(PaUtils.joinRecursive(adjustValueCount.entrySet(), delim, "[", "]"));
		b.append("], readSensorCount = [");
		b.append(PaUtils.joinRecursive(readSensorCount.entrySet(), delim, "[", "]"));
		b.append("]");
		return b.toString();
	}
	
	// This does deep copy of values as opposed to shallow copy, but feel free to optimize.
	public void copyFrom(IntervalPerVar other) {
		values.clear();
		for (Map.Entry<String, AbstractDomain> entry : other.values.entrySet()) {
			AbstractDomain n = new Domain();
			n.copyFrom(entry.getValue());
			values.put(entry.getKey(), n);
		}
	}
	
	public IntervalPerVar copy() {
		IntervalPerVar ipv = new IntervalPerVar();
		ipv.copyFrom(this);
		return ipv;
	}
	
	public static void join(IntervalPerVar src1, IntervalPerVar src2, IntervalPerVar trg) {
		for (Map.Entry<String, AbstractDomain> entry : src1.values.entrySet()) {
			trg.putIntervalForVar(entry.getKey(), entry.getValue().join(src2.getIntervalForVar(entry.getKey())).copy());
		}
	}
	
	public static void meet(IntervalPerVar src1, IntervalPerVar src2, IntervalPerVar trg) {
		for (Map.Entry<String, AbstractDomain> entry : src1.values.entrySet()) {
			trg.putIntervalForVar(entry.getKey(), entry.getValue().meet(src2.getIntervalForVar(entry.getKey())).copy());
		}
	}
	
	void putIntervalForVar(String var, AbstractDomain interval) {
		values.put(var, interval);
	}
	
	AbstractDomain getIntervalForVar(String var) {
		AbstractDomain i = values.get(var);
		if (i == null)
			return new Domain().getBot();
		else
			return i;
	}
	
	
	//TODO: rewrite this in a better way
	void putReadSensorCountForVar(String var, int sensorId, int count){
		if(!readSensorCount.containsKey(var)){
			readSensorCount.put(var, new HashMap<Integer, Integer>());
		}
		readSensorCount.get(var).put(sensorId, count);
	}
	
	//TODO: rewrite this in a better way
	int getReadSensorCountForVar(String var, int sensorId){
		if(!readSensorCount.containsKey(var) || readSensorCount.get(var).get(sensorId) == null){
			return 0;
		} else {
			return readSensorCount.get(var).get(sensorId);
		}
	}
	
	//TODO: rewrite this in a better way
	void putAdjustValueCountForVar(String var, int sensorId, int count){
		if(!adjustValueCount.containsKey(var)){
			adjustValueCount.put(var, new HashMap<Integer, Integer>());
		}
		adjustValueCount.get(var).put(sensorId, count);
	}
	
	//TODO: rewrite this in a better way
	int getAdjustValueCountForVar(String var, int sensorId){
		if(!adjustValueCount.containsKey(var) || adjustValueCount.get(var).get(sensorId) == null){
			return 0;
		} else {
			return adjustValueCount.get(var).get(sensorId);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntervalPerVar)) return false;
		return ((IntervalPerVar)o).values.equals(values);
	}

}
