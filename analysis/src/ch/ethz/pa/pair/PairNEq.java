package ch.ethz.pa.pair;

import ch.ethz.pa.AbstractDomain;
import ch.ethz.pa.Interval;

public class PairNEq extends Pair {

	public PairNEq(Interval l, Interval r) {
		super(l, r);
	}

	@Override
	public AbstractDomain apply() {
		return left.meet(right);
	};
}
