package ch.ethz.pa.pair;

import ch.ethz.pa.AbstractDomain;

public class PairEq extends Pair{

	public PairEq(AbstractDomain l, AbstractDomain r) {
		super(l, r);
	}
	
	@Override
	public AbstractDomain apply() {
		return left.meet(right);
	};

}
