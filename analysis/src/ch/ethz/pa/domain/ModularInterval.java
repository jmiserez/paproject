package ch.ethz.pa.domain;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

class ModularInterval extends Interval {
	
	/*
	 * This is designed according to the Bit-field paper by the 
	 * Abstract Domains for Bit-Level Machine Integer and Floating-point Operations paper by MinŽ
	 * 
	 */
	protected long l, h, k;
	protected boolean bot = false;
	
	private final static ModularInterval TOP = new ModularInterval(Integer.MIN_VALUE, Integer.MAX_VALUE);
	private final static ModularInterval BOT = new ModularInterval();
	
	public ModularInterval() {
		bot = true;
	}
	
	public ModularInterval(int const_val) {
		l = h = const_val;
	}

	public ModularInterval(int l, int h) {
		this.l = l;
		this.h = h;
	}
	
	private ModularInterval(long l, long h, long k) {
		this.l = l;
		this.h = h;
		this.k = k;
	}
	
	@Override
	public String toString() {
		if (this.equals(BOT)) {
			return "[BOT]";
		} else if (this.equals(TOP)) {
			return "[TOP]";
		}
		return String.format("[%d,%d]", l, h);
	}

	@Override
	public void copyFrom(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		l = i.l;
		h = i.h;
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
		ModularInterval i = (ModularInterval)o;
		if (this.isBot() && i.isBot())
			return true;
		if (this.isBot() || i.isBot())
			return false;
		if (this.isTop() && i.isTop())
			return true;
		return l == i.l && h == i.h;
	}
	
	private static long gcd(long k2, long k3) {
		return k3==0 ? k2 : gcd(k3,k2 % k3);
	}

	@Override
	public AbstractDomain join(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.equals(BOT))
			return i.copy();
		if (i.equals(BOT))
			return this.copy();
		return new ModularInterval(Math.min(this.l, i.l), Math.max(this.h, i.h), gcd(this.k, i.k));
	}

	@Override
	public AbstractDomain meet(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.equals(BOT))
			return BOT.copy();
		if (i.equals(BOT))
			return BOT.copy();
		if (this.l > i.h || i.l > this.h)
			return BOT.copy();
		return new ModularInterval((int) Math.max(this.l, i.l), (int) Math.min(this.h, i.h), gcd(this.k, i.k));
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
		return l == ModularInterval.TOP.l && h == ModularInterval.TOP.h;
	}

	@Override
	public boolean isBot() {
		return l == ModularInterval.BOT.l && h == ModularInterval.BOT.h && bot == true;
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
		ModularInterval i = (ModularInterval) other;
		return super.plus(other);
	}

	@Override
	public AbstractDomain minus(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		return super.minus(other);
	}

	@Override
	public AbstractDomain multiply(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		return super.multiply(other);
	}

	@Override
	public AbstractDomain divide(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		return super.divide(other);
	}

	@Override
	public AbstractDomain and(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.and(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain or(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.or(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain xor(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.xor(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain neg() {
		return super.neg();
	}

	@Override
	public AbstractDomain shl(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.shl(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain shr(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.shr(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain ushr(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.ushr(other);
		return TOP.copy();
	}

	@Override
	public AbstractDomain rem(AbstractDomain other) {
		ModularInterval i = (ModularInterval) other;
		if (this.k == 0 &&  i.k == 0)
			return super.rem(other);
		return TOP.copy();
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
