package ch.ethz.pa.domain;

/*
 * Public domain class, this is the class we create objects of.
 */
public final class Domain extends Interval {

	public Domain() {
		super();
	}
	
	public Domain(int value) {
		super(value);
	}

	public Domain(int i, int j) {
		super(i, j);
	}
	
	public int getLower(){
		return (int) lower;
	}
	
	public int getUpper(){
		return (int) upper;
	}

}
