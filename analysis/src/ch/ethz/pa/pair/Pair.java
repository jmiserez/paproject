package ch.ethz.pa.pair;

import ch.ethz.pa.Interval;

public abstract class Pair {
	protected Interval left, right;
	
	public Pair(Interval l, Interval r) {
		left = l;
		right = r;
	}
	public Interval getLeft() {
		return left;
	}
	
	public Interval getRight() {
		return right;
	}

	public abstract Interval apply();
}
