package ch.ethz.pa;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.pa.domain.AbstractDomain;
import ch.ethz.pa.domain.Domain;
import ch.ethz.pa.util.PaUtils;

public class IntervalPerVar {
	
	private HashMap<String, AbstractDomain> values;
	private HashMap<String, HashMap<Integer, Integer>> adjustValueCount;
	private HashMap<String, HashMap<Integer, Integer>> readSensorCount;
	
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
		b.append(PaUtils.prettyPrintIterable(values.entrySet(), delim));
		b.append("], adjustValueCount = [");
		b.append(PaUtils.prettyPrintIterableRecursive(adjustValueCount.entrySet(), delim, "[", "]"));
		b.append("], readSensorCount = [");
		b.append(PaUtils.prettyPrintIterableRecursive(readSensorCount.entrySet(), delim, "[", "]"));
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
		adjustValueCount = PaUtils.deepCopyMap(other.adjustValueCount);
		readSensorCount = PaUtils.deepCopyMap(other.readSensorCount);
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
		joinCounts(src1.adjustValueCount,src2.adjustValueCount,trg.adjustValueCount);
		joinCounts(src1.readSensorCount,src2.readSensorCount,trg.readSensorCount);
	}
	
	public static void meet(IntervalPerVar src1, IntervalPerVar src2, IntervalPerVar trg) {
		for (Map.Entry<String, AbstractDomain> entry : src1.values.entrySet()) {
			trg.putIntervalForVar(entry.getKey(), entry.getValue().meet(src2.getIntervalForVar(entry.getKey())).copy());
		}
		meetCounts(src1.adjustValueCount,src2.adjustValueCount,trg.adjustValueCount);
		meetCounts(src1.readSensorCount,src2.readSensorCount,trg.readSensorCount);
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
	
	void putReadSensorCountForVar(String var, int sensorId, int count){
		if(!readSensorCount.containsKey(var)){
			readSensorCount.put(var, new HashMap<Integer, Integer>());
		}
		readSensorCount.get(var).put(sensorId, count);
	}
	
	int getReadSensorCountForVar(String var, int sensorId){
		if(!readSensorCount.containsKey(var) || readSensorCount.get(var).get(sensorId) == null){
			return 0;
		} else {
			return readSensorCount.get(var).get(sensorId);
		}
	}
	
	void putAdjustValueCountForVar(String var, int sensorId, int count){
		if(!adjustValueCount.containsKey(var)){
			adjustValueCount.put(var, new HashMap<Integer, Integer>());
		}
		adjustValueCount.get(var).put(sensorId, count);
	}
	
	int getAdjustValueCountForVar(String var, int sensorId){
		if(!adjustValueCount.containsKey(var) || adjustValueCount.get(var).get(sensorId) == null){
			return 0;
		} else {
			return adjustValueCount.get(var).get(sensorId);
		}
	}
	
	private static void joinCounts(HashMap<String, HashMap<Integer, Integer>> src1, HashMap<String, HashMap<Integer, Integer>> src2, HashMap<String, HashMap<Integer, Integer>> trg){
		for (Map.Entry<String, HashMap<Integer,Integer>> entry: src1.entrySet()){
			HashMap<Integer,Integer> entry2Value = src2.get(entry.getKey()); //may be null
			for(Map.Entry<Integer, Integer> inner: entry.getValue().entrySet()){
				Integer sensorId = inner.getKey();
				Integer count = inner.getValue();
				trg.put(entry.getKey(), new HashMap<Integer, Integer>());
				if(entry2Value != null && entry2Value.containsKey(sensorId)){
					Integer count2 = entry2Value.get(sensorId);
					trg.get(entry.getKey()).put(sensorId, Math.max(count, count2));
				} else {
					trg.get(entry.getKey()).put(sensorId, count); //TODO is this correct?
				}
			}
		}
	}
	
	private static void meetCounts(HashMap<String, HashMap<Integer, Integer>> src1, HashMap<String, HashMap<Integer, Integer>> src2, HashMap<String, HashMap<Integer, Integer>> trg){
		for (Map.Entry<String, HashMap<Integer,Integer>> entry: src1.entrySet()){
			HashMap<Integer,Integer> entry2Value = src2.get(entry.getKey()); //may be null
			for(Map.Entry<Integer, Integer> inner: entry.getValue().entrySet()){
				Integer sensorId = inner.getKey();
				Integer count = inner.getValue();
				if(entry2Value != null && entry2Value.containsKey(sensorId)){
					Integer count2 = entry2Value.get(sensorId);
					trg.put(entry.getKey(), new HashMap<Integer, Integer>());
					trg.get(entry.getKey()).put(sensorId, Math.min(count, count2)); //TODO verify if min is correct
				} else {
					//TODO verify if this is correct: do not put
				}
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntervalPerVar)) return false;
		IntervalPerVar other = (IntervalPerVar) o;
		return other.values.equals(values) && other.adjustValueCount.equals(adjustValueCount) && other.readSensorCount.equals(readSensorCount);
	}

}
