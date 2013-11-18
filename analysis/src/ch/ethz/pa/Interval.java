package ch.ethz.pa;

import ch.ethz.pa.pair.Pair;
import ch.ethz.pa.pair.PairEq;

public class Interval {
	// TODO: Do you need to handle infinity or empty interval?
	private final static int INF = 10;
	
	protected int lower, upper;
	protected boolean bot = false;
	
	public Interval() {
		bot = true;
	}
	
	public Interval(int start_value) {
		lower = upper = start_value;
	}
	
	public Interval(int l, int u) {
		lower = l;
		upper = u;
	}
	
	@Override
	public String toString() {
		return String.format("[%d,%d]", lower, upper);
	}
	
	public void copyFrom(Interval other) {
		Interval i = (Interval) other;
		lower = i.lower;
		upper = i.upper;
	}

	private static Interval bounded(Interval i) {
		i.lower = Math.max(i.lower, -INF);
		i.upper = Math.min(i.upper, INF);
		return i;
	}
	
	private void set(int min, int max) {
		this.lower = min;
		this.upper = max;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval)) return false;
		Interval i = (Interval)o;
		if (this.isBot() && i.isBot())
			return true;
		if (this.isBot() ^ i.isBot())
			return false;
		return lower == i.lower && upper == i.upper;
	}

	private boolean isBot() {
		return lower > upper;
	}

	public Interval plus(Interval a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		// TODO: Handle overflow. 
		return bounded(new Interval(this.lower + i.lower, this.upper + i.upper));
	}

	public Interval minus(Interval a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		// TODO: Handle overflow. 
		return bounded(new Interval(this.lower - i.lower, this.upper - i.upper));
	}

	public Interval multiply(Interval a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		// TODO: Handle overflow.
		int newLower = this.lower * i.lower, 
			newUpper = this.upper * i.upper;
		// To handle case [-1, 1] * [-1, 1]
		if (this.lower < 0 && i.lower < 0 && (this.upper >= 0 || i.upper >= 0))
			newLower *= -1;
		// To handle case [-2, -1] * [-2, -1]
		if (this.upper < 0 && i.upper < 0 && (this.upper >= 0 || i.upper >= 0))
			newUpper *= -1;
		return bounded(new Interval(newLower, newUpper));
	}

	public Interval join(Interval a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		if (this.equals(getBot()))
			return i;
		if (i.equals(getBot()))
			return this;
		return bounded(new Interval(Math.min(this.lower, i.lower), Math.max(this.upper, i.upper)));
	}

	public Interval meet(Interval a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		if (this.equals(getBot()))
			return getBot();
		if (i.equals(getBot()))
			return getBot();
		if (this.lower > i.upper || i.lower > this.upper)
			return null;
		return new Interval(Math.max(this.lower, i.lower), Math.min(this.upper, i.upper));
	}

	public Interval getTop() {
		return new Interval(-INF, INF);
	}

	public Interval getBot() {
		return new Interval();
	}
	
	
	
}
