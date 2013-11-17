package ch.ethz.pa;

public class Interval {
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
		lower = other.lower;
		upper = other.upper;
	}
	
	public static Interval plus(Interval i1, Interval i2) {
		// TODO: Handle overflow. 
		return bounded(new Interval(i1.lower +i2.lower, i1.upper + i2.upper));
	}

	private static Interval bounded(Interval i) {
		i.lower = Math.max(i.upper, -INF);
		i.upper = Math.min(i.upper, INF);
		return i;
	}

	public static Interval minus(Interval i1, Interval i2) {
		// TODO: Handle overflow. 
		return bounded(new Interval(i1.lower - i2.lower, i1.upper - i2.upper));
	}

	public static Interval multiply(Interval i1, Interval i2) {
		// TODO: Handle overflow. 
		int newLower = i1.lower * i2.lower, 
				newUpper = i1.upper * i2.upper;
		// To handle case [-1, 1] * [-1, 1]
		if (i1.lower < 0 && i2.lower < 0)
			newLower *= -1;
		// To handle case [-2, -1] * [-2, -1]
		if (i1.upper < 0 && i2.upper < 0)
			newUpper *= -1;
		return bounded(new Interval(newLower, newUpper));
	}
	
	public Interval join(Interval o) {
		if (o == null)
			return TOP;
		return bounded(new Interval(Math.min(this.lower, o.lower), Math.max(this.upper, o.upper)));
	}

	private void set(int min, int max) {
		this.lower = min;
		this.upper = max;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval)) return false;
		Interval i = (Interval)o;
		return lower == i.lower && upper == i.upper;
	}

	// TODO: Do you need to handle infinity or empty interval?
	int lower, upper;
	
	final static int INF = 1000;
	final static Interval TOP = new Interval(-INF, INF);
}
