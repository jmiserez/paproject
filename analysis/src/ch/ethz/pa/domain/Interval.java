package ch.ethz.pa.domain;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

/* This is default on purpose, use Domain for creating objects
 * or AbstractDomain as type for variables.
 */
class Interval extends AbstractDomain {
	
	// TODO: Do you need to handle infinity or empty interval?
	private final static int MIN_INF = -9999; //TODO handle infinity better
	private final static int MAX_INF = 9999; 
	private final static Interval TOP = new Interval(-MIN_INF, MAX_INF);
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
		if (i.lower < MIN_INF || i.upper > MAX_INF)
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
		if (this.isBot() || i.isBot())
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
	
	public AbstractDomain divide(AbstractDomain a) {
//		// Note: we do not check for division by 0
//		// division by -1 can negate the number, otherwise the number can only get smaller
//		Interval i = (Interval) a;
//		int newLower;
//		int newUpper;
//		if(i.lower < 0 && i.upper >= 0){
//			//contains -1
//			newLower = Math.min(Math.min(-this.lower, this.lower),Math.min(-this.upper, this.upper));
//			newUpper = Math.max(Math.max(-this.lower, this.lower),Math.max(-this.upper, this.upper));
//		}
//		
		
		//TODO do properly
		return TOP.copy(); //TODO implement;
//		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain rem(AbstractDomain a) {
		// Note: we do not check for division by 0
		// this % a
		// result is negative iff this is < 0 (sign of a has no influence)
		// abs(result) is always smaller than a
		Interval i = (Interval) a;

		int maxAbsRemainder = Math.max(0, Math.max(Math.abs(i.lower), Math.abs(i.upper))-1);
		
		int newUpper = maxAbsRemainder;
		if(this.upper < maxAbsRemainder){
			newUpper = this.upper;
		}

		// negative remainders only happen for negative this
		int thisMin = Math.min(0, Math.min(this.lower, this.upper));
			
		int newLower = -maxAbsRemainder; //default case
		if(thisMin < -maxAbsRemainder){
			newLower = -thisMin; // case where a is larger then this, e.g. 5 % 20
		}
		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain neg() {
		int newLower = -this.lower;
		int newUpper = -this.upper;
		
		//TODO: is this true even with overflow?
		if(newUpper < newLower){
			//swap values
			newLower = newLower ^ newUpper;
			newUpper = newLower ^ newUpper;
			newLower = newLower ^ newUpper;
		}
		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain and(AbstractDomain a) {
		return TOP.copy(); //TODO implement
//		Interval i = (Interval) a;
//		
//		List<Integer> newBounds = new ArrayList<Integer>();
//		int[] thisBounds = {this.lower, this.upper};
//		int[] iBounds = {i.lower, i.upper};
//
//		// 0: -/- -> intersection = [X, -1] (X is calculated as seen below)
//		// 1: -/+ -> intersection = [0,0]
//		// 2: +/- -> intersection = [0,0]
//		// 3: +/+ -> intersection = [0, Y] (Y is calculated as seen below)
//		
//		for(int tBound : thisBounds){
//			for(int iBound : iBounds){
//				if(tBound < 0 && iBound < 0){
//					newBounds.add(~((1 << (Integer.numberOfLeadingZeros(Math.max(~tBound, ~iBound))+1)) - 1));
//					newBounds.add(-1);
//				}
//				// case 2: this negative, i positive
//				if(tBound < 0 && iBound >= 0){
//					newBounds.add(0);
//				}
//				// case 3: this positive, i negative
//				if(tBound < 0 && iBound >= 0){
//					newBounds.add(0);
//				}
//				// case 4: both positive. AND values of intersection go up from 0 to this value
//				if(tBound < 0 && iBound < 0){
//					newBounds.add(0);
//					newBounds.add((1 << (Integer.numberOfLeadingZeros(Math.max(tBound, iBound))+1)) - 1);
//				}
//			}
//		}
//		if(newBounds.size() < 1){
//			return TOP;
//		}
//		return handleOverflow(new Interval(Collections.min(newBounds), Collections.max(newBounds)));
	}

	public AbstractDomain or(AbstractDomain a) {
		return TOP;
//		Interval i = (Interval) a;
//
//		List<Integer> newBounds = new ArrayList<Integer>();
//		int[] thisBounds = {this.lower, this.upper};
//		int[] iBounds = {i.lower, i.upper};
//
//		// 0: -/- -> intersection = [X, -1] (X is calculated as seen below)
//		// 1: -/+ -> intersection = [0,0]
//		// 2: +/- -> intersection = [0,0]
//		// 3: +/+ -> intersection = [0, Y] (Y is calculated as seen below)
//		
//		for(int tBound : thisBounds){
//			for(int iBound : iBounds){
//				if(tBound < 0 && iBound < 0){
//					newBounds.add(~((1 << (Integer.numberOfLeadingZeros(Math.max(~tBound, ~iBound)))+1) - 1));
//					newBounds.add(-1);
//				}
//				// case 2: this negative, i positive
//				if(tBound < 0 && iBound >= 0){
//					//TODO
//				}
//				// case 3: this positive, i negative
//				if(tBound < 0 && iBound >= 0){
//					//TODO
//				}
//				// case 4: both positive. AND values of intersection go up from 0 to this value
//				if(tBound < 0 && iBound < 0){
//					newBounds.add(0);
//					// 0000 0111
//					// 0001 0000
//					// 0001 0000
//					newBounds.add((1 << (Integer.numberOfLeadingZeros(Math.max(tBound, iBound)) + 1))
//							+ ((1 << (Integer.numberOfLeadingZeros(Math.min(tBound, iBound)) + 1)) - 1));
//					
//				}
//			}
//		}
//		if(newBounds.size() < 1){
//			return TOP;
//		}
//		return handleOverflow(new Interval(Collections.min(newBounds), Collections.max(newBounds)));
	}

	public AbstractDomain xor(AbstractDomain a) {
		return TOP.copy(); //TODO implement
	}

	public AbstractDomain shl(AbstractDomain a) {
		return TOP.copy(); //TODO implement
	}

	public AbstractDomain shr(AbstractDomain a) {
		return TOP.copy(); //TODO implement
	}
	
	public AbstractDomain ushr(AbstractDomain a) {
		return TOP.copy(); //TODO implement
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

		private final static AbstractDomain pInf = new Interval(MIN_INF, MIN_INF); // -Infinity
		private final static AbstractDomain mInf = new Interval(MAX_INF, MAX_INF); // +Infinity
		private final static AbstractDomain one = new Interval(1, 1); // 1
		
		/* 
		 *  Implement the different pairs below
		 */
		public PairSwitch(AbstractDomain i1, AbstractDomain i2) {
			this.i1 = (Interval) i1;
			this.i2 = (Interval) i2;
		}
		
		/*
		 * There is test cases for these in DomainTest
		 */

		@Override
		Pair<AbstractDomain, AbstractDomain> doEqExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2), i1.meet(i2));
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doNeExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.join(i2), i1.join(i2));
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doLeExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2.join(pInf)), i1.join(mInf).meet(i2));
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doGeExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2.join(mInf)), i1.join(pInf).meet(i2));
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doGtExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i2.plus(one).join(mInf).meet(i1), i1.minus(one).join(pInf).meet(i2));
		}

		@Override
		Pair<AbstractDomain, AbstractDomain> doLtExpr(ConditionExpr v) {
			return new Pair<AbstractDomain, AbstractDomain>(i1.meet(i2.minus(one).join(pInf)), i1.plus(one).join(mInf).meet(i2));
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
		return lower == Interval.BOT.lower && upper == Interval.BOT.upper && bot == true;
	}

}
