package ch.ethz.pa.domain;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

public class ModularInterval extends AbstractDomain {
	
	/*
	 * The interval between lower and upper is the intresting case. I.e. 5 is in [1,8] and not in [8,1].
	 */
	protected long lower, upper;
	protected boolean bot = false;
	
	private final static ModularInterval TOP = new ModularInterval(Integer.MIN_VALUE, Integer.MAX_VALUE);
	private final static ModularInterval BOT = new ModularInterval();
	
	public ModularInterval() {
		bot = true;
	}
	
	public ModularInterval(int start_value) {
		lower = upper = start_value;
	}
	
	public ModularInterval(int l, int u) {
		lower = l;
		upper = u;
	}

	@Override
	public void copyFrom(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		lower = i.lower;
		upper = i.upper;
		bot = i.bot;
	}

	@Override
	public AbstractDomain copy() {
		ModularInterval i = new ModularInterval();
		i.copyFrom(this);
		return i;
	}
	
	

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ModularInterval)) return false;
		Interval i = (Interval)o;
		if (this.isBot() && i.isBot())
			return true;
		if (this.isBot() || i.isBot())
			return false;
		return lower == i.lower && upper == i.upper;
	}
	
	private long size() {
		if (this.isBot()) {
			return 0;
		} else if (lower < upper) {
			return upper - lower;
		} else {
			return lower - upper;
		}
	}

	@Override
	public AbstractDomain join(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.equals(BOT))
			return i.copy();
		if (i.equals(BOT))
			return this.copy();
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain meet(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.equals(BOT))
			return BOT.copy();
		if (i.equals(BOT))
			return BOT.copy();
		if (this.lower > i.upper && i.lower > this.upper)
			return BOT.copy();
		// TODO Auto-generated method stub
		return ModularInterval.BOT.copy();
	}

	@Override
	public AbstractDomain getTop() {
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain getBot() {
		return ModularInterval.BOT.copy();
	}

	@Override
	public boolean isTop() {
		return lower == ModularInterval.TOP.lower && upper == ModularInterval.TOP.upper;
	}

	@Override
	public boolean isBot() {
		return lower == ModularInterval.BOT.lower && upper == ModularInterval.BOT.upper && bot == true;
	}

	@Override
	public int diff(AbstractDomain other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AbstractDomain widen(int directionality) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}
	
	// ---- TRANSFORMERS ----

	@Override
	public AbstractDomain plus(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain minus(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain multiply(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain divide(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain and(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain or(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain xor(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain neg() {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain shl(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain shr(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain ushr(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}

	@Override
	public AbstractDomain rem(AbstractDomain other) {
		// TODO Auto-generated method stub
		return ModularInterval.TOP.copy();
	}
	
	// ---- PAIRS ----
	
	public static class PairSwitch extends AbstractDomain.PairSwitch {
		ModularInterval i1, i2;

		//private final static AbstractDomain pInf = new ModularInterval(MIN_INF, MIN_INF); // -Infinity
		//private final static AbstractDomain mInf = new ModularInterval(MAX_INF, MAX_INF); // +Infinity
		private final static AbstractDomain one = new ModularInterval(1, 1); // 1
		
		/* 
		 *  Implement the different pairs below
		 */
		public PairSwitch(AbstractDomain i1, AbstractDomain i2) {
			this.i1 = (ModularInterval) i1;
			this.i2 = (ModularInterval) i2;
		}
		
		/*
		 * There is test cases for these in DomainTest
		 */

		@Override
		Pair<AbstractDomain, AbstractDomain> doEqExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doNeExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doLeExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doGeExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doGtExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doLtExpr(ConditionExpr v) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
