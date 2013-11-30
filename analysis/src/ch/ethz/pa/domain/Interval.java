package ch.ethz.pa.domain;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

/* This is default on purpose, use Domain for creating objects
 * or AbstractDomain as type for variables.
 */
class Interval extends AbstractDomain {
	// TODO: Do you need to handle infinity or empty interval?
	private final static int INF = 10;
	private final static Interval TOP = new Interval(-INF, INF);
	private final static Interval BOT = new Interval();
	
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
		if (this.equals(BOT)) {
			return "[BOT]";
		}
		return String.format("[%d,%d]", lower, upper);
	}

	@Override
	public void copyFrom(AbstractDomain other) {
		Interval i = (Interval) other;
		lower = i.lower;
		upper = i.upper;
		bot = i.bot;
	}

	private static AbstractDomain handleOverflow(Interval i) {
		// TODO: Be more precise
		if (i.lower < -INF || i.upper > INF)
			return TOP.copy();
		else
			return i;
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
	
	public AbstractDomain plus(AbstractDomain a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		// TODO: Handle overflow.
		return handleOverflow(new Interval(this.lower + i.lower, this.upper + i.upper));
	}

	public AbstractDomain minus(AbstractDomain a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		// TODO: Handle overflow. 
		return handleOverflow(new Interval(this.lower - i.lower, this.upper - i.upper));
	}

	public AbstractDomain multiply(AbstractDomain a) {
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
		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	@Override
	public AbstractDomain join(AbstractDomain a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		if (this.equals(BOT))
			return i.copy();
		if (i.equals(BOT))
			return this.copy();
		return handleOverflow(new Interval(Math.min(this.lower, i.lower), Math.max(this.upper, i.upper)));
	}
	
	@Override
	public AbstractDomain meet(AbstractDomain a) {
		// Cross fingers and hope a is instanceof Interval
		Interval i = (Interval) a;
		if (this.equals(BOT))
			return BOT.copy();
		if (i.equals(BOT))
			return BOT.copy();
		if (this.lower > i.upper || i.lower > this.upper)
			return BOT.copy();
		return new Interval(Math.max(this.lower, i.lower), Math.min(this.upper, i.upper));
	}

	@Override
	public AbstractDomain copy() {
		Interval i = new Interval();
		i.copyFrom(this);
		return i;
	}
	
	public static class PairSwitch extends AbstractDomain.PairSwitch {
		Interval i1, i2;

		private final static AbstractDomain pInf = new Interval(-INF, -INF); // +Infinity
		private final static AbstractDomain mInf = new Interval(INF, INF); // -Infinity
		
		/* 
		 *  Implement the different pairs below
		 */
		public PairSwitch(AbstractDomain i1, AbstractDomain i2) {
			this.i1 = (Interval) i1;
			this.i2 = (Interval) i2;
		}
		
		Pair<AbstractDomain, AbstractDomain> doEqExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2), i1.meet(i2));
		}
		
		Pair<AbstractDomain, AbstractDomain> doNeExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.join(i2), i1.join(i2));
		}
		
		Pair<AbstractDomain, AbstractDomain> doLeExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2.join(mInf)), i1.join(pInf).meet(i2));
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

	@Override
	public AbstractDomain getTop() {
		return Interval.TOP.copy();
	}

	@Override
	public AbstractDomain getBot() {
		return Interval.BOT.copy();
	}

	@Override
	public boolean isTop() {
		return lower == Interval.TOP.lower && upper == Interval.TOP.upper;
	}

	@Override
	public boolean isBot() {
		return lower == Interval.BOT.lower && upper == Interval.BOT.upper;
	}
}
