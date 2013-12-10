package ch.ethz.pa.domain;

/*
 * Public domain class, this is the class we create objects of.
 */
public final class Domain extends ModularInterval {

	public Domain() {
		super();
	}
	
	public Domain(int value) {
		super(value);
	}

	public Domain(int i, int j) {
		super(i, j);
	}

}
