package ch.ethz.pa.domain;

import java.util.ArrayList;
import java.util.Collections;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

/* This is default on purpose, use Domain for creating objects
 * or AbstractDomain as type for variables.
 */
class Interval extends AbstractDomain {
	
	// TODO: Do you need to handle infinity or empty interval?
	private final static long MIN_VALUE = Integer.MIN_VALUE;
	private final static long MAX_VALUE =  Integer.MAX_VALUE; 
	private final static long RANGE =  MAX_VALUE - MIN_VALUE + 1;
	private final static Interval TOP = new Interval(MIN_VALUE, MAX_VALUE);
	private final static Interval BOT = new Interval();
	
	private final static int DIFF_LOWER_DECREASING = 1 << 0;
	private final static int DIFF_LOWER_INCREASING = 1 << 1;
	private final static int DIFF_UPPER_DECREASING = 1 << 2;
	private final static int DIFF_UPPER_INCREASING = 1 << 3;
	
	protected long lower, upper;
	protected boolean bot = false;
	
	public Interval() {
		bot = true;
	}
	
	public Interval(long start_value) {
		lower = upper = start_value;
	}
	
	public Interval(long l, long u) {
		lower = l;
		upper = u;
	}
	
	@Override
	public String toString() {
		if (this.equals(BOT)) {
			return "[BOT]";
		}
		if (this.equals(TOP)) {
			return "[TOP]";
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
		if (i.lower > i.upper){
			return TOP.copy();
		}
		if (i.lower < MIN_VALUE || i.upper > MAX_VALUE) {
			long length = i.upper - i.lower;
			if(length <= RANGE && length >= 0){
				if(i.lower < MIN_VALUE && i.upper < MIN_VALUE){
					while(i.lower < MIN_VALUE){ //TODO: use modulo here instead of a loop
						i.lower += RANGE;
						i.upper += RANGE;
					}
					return i;
				} else if(i.lower > MAX_VALUE && i.upper > MAX_VALUE){
					while(i.lower > MAX_VALUE){ //TODO: use modulo here instead of a loop
						i.lower -= RANGE;
						i.upper -= RANGE;
					}
					return i;
				}
				//this happens if the interval crosses a boundary
				return TOP.copy();
			}
			return TOP.copy();
		} else {
			return i;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval)) return false;
		Interval i = (Interval)o;
		if(isBot() && i.isBot()){
			return true;
		}
		if(isBot() || i.isBot()){
			return false;
		}
		return lower == i.lower && upper == i.upper;
	}
	
	public AbstractDomain plus(AbstractDomain a) {
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		return handleOverflow(new Interval(this.lower + Math.min(i.lower, i.upper), this.upper + Math.max(i.lower, i.upper)));
	}

	public AbstractDomain minus(AbstractDomain a) {
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		return handleOverflow(new Interval(this.lower - Math.max(i.lower, i.upper), this.upper - Math.min(i.lower, i.upper)));
	}

	public AbstractDomain multiply(AbstractDomain a) {
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		// TODO: Handle overflow.
		long newLower = this.lower * i.lower, 
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
		// Note: we do not check for division by 0
		// division by negative numbers negate the result
		// [500, 1000] / [2,4] = [500/4, 1000/2]
		// [-300, 100] / [-2,4] = [-300/1, -300/-1] = [-300, 300]
		// [-300, 100] / [2,4] = [-300/2, 100/2] = [-150, 50]
		// [-300, 100] / [-2,-4] = [100/-2, -300/-2] = [-50, 150]
		
		// to make this bullet-proof, it seems like we can just compare all the possibilities, with an extra case for -1 and 1
		// TODO: figure out the underlying logic and implement without candidate list
		
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		ArrayList<Long> candidates = new ArrayList<Long>(8);
		long newLower;
		long newUpper;
		if(i.lower <= -1 && i.upper >= -1){
			//contains -1
			candidates.add(this.lower/-1);
			candidates.add(this.upper/-1);
		}
		if(i.lower <= 1 && i.upper >= 1){
			//contains 1
			candidates.add(this.lower/1);
			candidates.add(this.upper/1);
		}
		candidates.add(this.lower/i.lower);
		candidates.add(this.upper/i.lower);
		candidates.add(this.lower/i.upper);
		candidates.add(this.upper/i.upper);
		
		newLower = Collections.min(candidates);
		newUpper = Collections.max(candidates);
		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain rem(AbstractDomain a) {
		// Note: we do not check for division by 0
		// this % a
		// result is negative iff this is < 0 (sign of a has no influence)
		// abs(result) is always smaller than a
		//TODO: check if this is actually correct
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}

		long maxAbsRemainder = Math.max(0, Math.max(Math.abs(i.lower), Math.abs(i.upper))-1);
		
		long newUpper = maxAbsRemainder;
		if(this.upper < maxAbsRemainder){
			newUpper = this.upper;
		}

		// negative remainders only happen for negative this
		long thisMin = Math.min(0, Math.min(this.lower, this.upper));
			
		long newLower = -maxAbsRemainder; //default case
		if(thisMin < -maxAbsRemainder){
			newLower = -thisMin; // case where a is larger then this, e.g. 5 % 20
		}
		return handleOverflow(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain neg() {
		if(isTop()){
			return TOP.copy();
		}
		long newLower = -this.lower;
		long newUpper = -this.upper;
		
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
//		long[] thisBounds = {this.lower, this.upper};
//		long[] iBounds = {i.lower, i.upper};
//
//		// 0: -/- -> intersection = [X, -1] (X is calculated as seen below)
//		// 1: -/+ -> intersection = [0,0]
//		// 2: +/- -> intersection = [0,0]
//		// 3: +/+ -> intersection = [0, Y] (Y is calculated as seen below)
//		
//		for(long tBound : thisBounds){
//			for(long iBound : iBounds){
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
		return TOP.copy();
//		Interval i = (Interval) a;
//
//		List<Integer> newBounds = new ArrayList<Integer>();
//		long[] thisBounds = {this.lower, this.upper};
//		long[] iBounds = {i.lower, i.upper};
//
//		// 0: -/- -> intersection = [X, -1] (X is calculated as seen below)
//		// 1: -/+ -> intersection = [0,0]
//		// 2: +/- -> intersection = [0,0]
//		// 3: +/+ -> intersection = [0, Y] (Y is calculated as seen below)
//		
//		for(long tBound : thisBounds){
//			for(long iBound : iBounds){
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
		// Note: Shifting by x is actually defined as: 
		//    1 << x == 1 << (x & 0xf1)
		// as per: http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.19
		// ref: http://stackoverflow.com/questions/10516786/shifted-by-negative-number-in-java
		// 
		
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		//e.g. -56 to -29 or -64 to -33
		long normalizedLower = i.lower & 0x1f; //e.g. 8 or 0
		long normalizedRange = Math.min(31, i.upper - i.lower); //e.g 27 or 31
		long normalizedUpper = normalizedLower + normalizedRange; //e.g 8+27=35 or 0+31=31
		//normalized is a range somewhere in [0,31] e.g. [8, 35] or [0,31]
		// note: 1 << 8 == 1 << 8, but 1 << 35 == 1 << 3, so we actually have to deal with 2 intervals
		
		if(normalizedUpper > 31){
			long lower1 = normalizedLower;
			long upper1 = 31;
			long lower2 = 0;
			long upper2 = normalizedUpper % 32;
			Interval interval1 = (Interval) multiply(new Interval(1 << lower1,  1 << upper1));
			Interval interval2 = (Interval) multiply(new Interval(1 << lower2,  1 << upper2));
			return handleOverflow(new Interval(Math.min(interval1.lower, interval2.lower), Math.max(interval1.upper, interval2.upper)));
		} else {
			return multiply(new Interval(1 << normalizedLower,  1 << normalizedUpper));
		}
	}

	public AbstractDomain shr(AbstractDomain a) {
//		Interval i = (Interval) a;
//		if(isTop() || i.isTop()){
//			return TOP.copy();
//		}
//		return divide(new Interval(1 << i.lower,  1 << i.upper));
		return TOP.copy();
	}
	
	public AbstractDomain ushr(AbstractDomain a) {
		return TOP.copy(); //TODO implement
	}
	
	@Override
	public AbstractDomain join(AbstractDomain a) {
		Interval i = (Interval) a;
		if (this.equals(BOT))
			return i.copy();
		if (i.equals(BOT))
			return this.copy();
		return handleOverflow(new Interval(Math.min(this.lower, i.lower), Math.max(this.upper, i.upper)));
	}
	
	@Override
	public AbstractDomain meet(AbstractDomain a) {
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

		private final static AbstractDomain pInf = new Interval(MIN_VALUE, MIN_VALUE); // -Infinity
		private final static AbstractDomain mInf = new Interval(MAX_VALUE, MAX_VALUE); // +Infinity
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
		return lower == Interval.BOT.lower && upper == Interval.BOT.upper  && bot == true;
	}
	
	@Override
	public int diff(AbstractDomain a) {
		int directionality = 0;
		Interval i = (Interval) a;
		if(i.lower < this.lower){
			directionality |= DIFF_LOWER_DECREASING;
		}
		if(i.lower > this.lower){
			directionality |= DIFF_LOWER_INCREASING;
		}
		if(i.upper < this.upper){
			directionality |= DIFF_UPPER_DECREASING;
		}
		if(i.upper > this.upper){
			directionality |= DIFF_UPPER_INCREASING;
		}
		return directionality;
	}
	
	@Override
	public AbstractDomain widen(int directionality) {
		Interval result = (Interval) this.copy();
		return TOP.copy();
	}

}
