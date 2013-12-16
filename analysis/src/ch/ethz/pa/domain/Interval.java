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

	private static AbstractDomain moveIntoRange(Interval i) {
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
				//return TOP.copy();
			}
			return TOP.copy();
		} else {
			return i;
		}
	}
	
	private void handleOverflow(Interval i) {
		if (i.lower < MIN_VALUE || i.upper > MAX_VALUE) {
			i.copyFrom(TOP);
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
		return moveIntoRange(new Interval(this.lower + Math.min(i.lower, i.upper), this.upper + Math.max(i.lower, i.upper)));
	}

	public AbstractDomain minus(AbstractDomain a) {
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		return moveIntoRange(new Interval(this.lower - Math.max(i.lower, i.upper), this.upper - Math.min(i.lower, i.upper)));
	}

	public AbstractDomain multiply(AbstractDomain a) {
		Interval i = (Interval) a.copy();
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower * i.lower));
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
		return moveIntoRange(new Interval(newLower, newUpper));
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
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower / i.lower));
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
		return moveIntoRange(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain rem(AbstractDomain a) {
		// Note: we do not check for division by 0
		// this % a
		// result is negative iff this is < 0 (sign of a has no influence)
		// abs(result) is always smaller than a
		//TODO: check if this is actually correct
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower % i.lower));
		}
		if(this.upper - this.lower == 0 && this.upper == 0){
			//this is 0
			return moveIntoRange(new Interval(0));
		}
		
		long range = this.upper - this.lower;

		long maxAbsRemainder = Math.max(0, Math.max(Math.abs(i.lower), Math.abs(i.upper))-1);
		
		long newUpper = maxAbsRemainder;
		if(this.upper < maxAbsRemainder){
			//max possible remainder is > than number
			newUpper = this.upper;
		}
		long newLower = maxAbsRemainder * -1;
		if(this.lower >= 0){
			newLower = 0;
			if(this.lower < maxAbsRemainder){
				newLower = this.lower;
			}
		}
		
		return moveIntoRange(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain neg() {
		handleOverflow(this);
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
		return moveIntoRange(new Interval(newLower, newUpper));
	}
	
	
	private static long logAwayFromZero(long val){
		if(val < 0){
			// 1011 to 1000, bits are 0
			// 0100 -> 0111 -> 1000
			return ~((Long.highestOneBit(~val) << 1) - 1);
		} else {
			// 0101 to 0111, bits are 1
			return ((Long.highestOneBit(val) << 1) - 1);
		}
	}
	
	private static long logTowardsZero(long val){
		if(val < 0){
			return logAwayFromZero(val) >> 1;
		} else {
			return logAwayFromZero(val) << 1;
		}
	}
	
	@Override
	public AbstractDomain and(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower & i.lower));
		}
		
		ArrayList<Long> candidates = new ArrayList<Long>(8);
		long newLower;
		long newUpper;
		
		if(this.lower == 0 && this.upper == 0 || i.lower == 0 && i.upper == 0){
			return moveIntoRange(new Interval(0));
		} else if(this.lower == -1 && this.upper == -1){
			return moveIntoRange((Interval) i.copy());
		} else if(i.lower == -1 && i.upper == -1){
			return moveIntoRange((Interval) this.copy());
		} else if(this.lower >= 0 && i.lower == Integer.MIN_VALUE && i.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(0));
		} else if(i.lower >= 0 && this.lower == Integer.MIN_VALUE && this.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(0));
		} else if(this.upper <= -1 && i.lower == Integer.MAX_VALUE && i.upper == Integer.MAX_VALUE){
			//negative numbers 1... and 0...
			long max = this.upper & Integer.MAX_VALUE;
			long min = this.lower & Integer.MAX_VALUE;
			return moveIntoRange(new Interval(min,max));
		} else if(i.upper <= -1 && this.lower == Integer.MAX_VALUE && this.upper == Integer.MAX_VALUE){
			//negative numbers 1... and 0...
			long max = i.upper & Integer.MAX_VALUE;
			long min = i.lower & Integer.MAX_VALUE;
			return moveIntoRange(new Interval(min,max));
		} else if(this.lower >=0 && i.lower == Integer.MAX_VALUE && i.upper == Integer.MAX_VALUE){
			return moveIntoRange(new Interval(0));
		} else if(i.upper >= 0 && this.lower == Integer.MAX_VALUE && this.upper == Integer.MAX_VALUE){
			return moveIntoRange(new Interval(0));
		} else if(this.lower >= 0 && i.lower >= 0){
			//only positive numbers
			candidates.add(0L);  //AND can go this far down
			candidates.add(this.upper & logAwayFromZero(i.upper));
			candidates.add(i.upper & logAwayFromZero(this.upper));
		} else if(this.upper <= -1 && i.upper <= -1){
			//only negative numbers
			candidates.add(~0L);  //AND can go this far up
			candidates.add(this.lower & logAwayFromZero(i.lower));
			candidates.add(i.lower & logAwayFromZero(this.lower));
		} else {
			return TOP.copy();
		}
		
		newLower = Collections.min(candidates);
		newUpper = Collections.max(candidates);
		
		return moveIntoRange(new Interval(newLower, newUpper));
	}
	
	public AbstractDomain or(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower | i.lower));
		}
		
		ArrayList<Long> candidates = new ArrayList<Long>(8);
		long newLower;
		long newUpper;
		
		candidates.add(this.lower);
		candidates.add(this.upper);
		candidates.add(i.lower);
		candidates.add(i.upper);

		if(this.lower == -1 && this.upper == -1 || i.lower == -1 && i.upper == -1){
			return moveIntoRange(new Interval(-1));
		} else if(this.lower == 0 && this.upper == 0){
			return moveIntoRange((Interval) i.copy());
		} else if(i.lower == 0 && i.upper == 0){
			return moveIntoRange((Interval) this.copy());
		} else if(this.lower >= 0 && i.lower == Integer.MIN_VALUE && i.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(this.lower | Integer.MIN_VALUE, this.upper | Integer.MIN_VALUE));
		} else if(i.lower >= 0 && this.lower == Integer.MIN_VALUE && this.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(i.lower | Integer.MIN_VALUE, i.upper | Integer.MIN_VALUE));
		} else if(this.lower >= 0 && i.lower >= 0){
			//only positive numbers
			candidates.add(this.lower);
			candidates.add(i.lower);
			candidates.add(i.upper | logAwayFromZero(this.upper));
			candidates.add(this.upper | logAwayFromZero(i.upper));
		} else if(this.upper <= -1 && i.upper <= -1){
			//only negative numbers
			candidates.add(this.lower);
			candidates.add(i.lower);
			candidates.add(-1L);
		} else {
			return TOP.copy();
		}
		
		newLower = Collections.min(candidates);
		newUpper = Collections.max(candidates);
		
		return moveIntoRange(new Interval(newLower, newUpper));
	}

	public AbstractDomain xor(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(this.lower ^ i.lower));
		}
		
		ArrayList<Long> candidates = new ArrayList<Long>(8);
		long newLower;
		long newUpper;
		
		candidates.add(this.lower);
		candidates.add(this.upper);
		candidates.add(i.lower);
		candidates.add(i.upper);

		if(this.lower == -1 && this.upper == -1){
			return moveIntoRange(new Interval(~i.upper, ~i.lower));
		}else if(i.lower == -1 && i.upper == -1){
			return moveIntoRange(new Interval(~this.upper, ~this.lower));
		} else if(this.lower == 0 && this.upper == 0){
			return moveIntoRange((Interval) i.copy());
		} else if(i.lower == 0 && i.upper == 0){
			return moveIntoRange((Interval) this.copy());
		} else if(this.lower >= 0 && i.lower == Integer.MIN_VALUE && i.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(i.lower ^ Integer.MIN_VALUE ,i.upper ^ Integer.MIN_VALUE));
		} else if(i.lower >= 0 && this.lower == Integer.MIN_VALUE && this.upper == Integer.MIN_VALUE){
			//positive numbers 0... and 1...
			return moveIntoRange(new Interval(this.lower ^ Integer.MIN_VALUE ,this.upper ^ Integer.MIN_VALUE));
		} else if(this.upper <= -1 && i.lower == Integer.MAX_VALUE && i.upper == Integer.MAX_VALUE){
			//negative numbers 1... and 0...
			return moveIntoRange(new Interval(i.upper ^ Integer.MAX_VALUE ,i.lower ^ Integer.MAX_VALUE));
		} else if(i.upper <= -1 && this.lower == Integer.MAX_VALUE && this.upper == Integer.MAX_VALUE){
			//negative numbers 1... and 0...
			return moveIntoRange(new Interval(this.upper ^ Integer.MAX_VALUE ,this.lower ^ Integer.MAX_VALUE));
		} else if(this.lower >= 0 && i.lower >= 0){
			//only positive numbers
			candidates.add(0L);
			candidates.add(this.lower);
			candidates.add(i.lower);
			candidates.add(logAwayFromZero(this.upper));
			candidates.add(logAwayFromZero(i.upper));
		} else if(this.upper <= -1 && i.upper <= -1){
			//only negative numbers
			candidates.add((long)Integer.MAX_VALUE);
			candidates.add(0L);
		} else {
			return TOP.copy();
		}
		
		newLower = Collections.min(candidates);
		newUpper = Collections.max(candidates);
		
		return moveIntoRange(new Interval(newLower, newUpper));
	}
	
//	
//	The complete 8-bit 2's complement integer range.
//  ================================================
//	
//	Dealing with unsigned values for bitwise operations:
//	 - unsigned: result = a OP b
//	 - signed:   result = ~(~a OP ~b)
//	
//  1000 0000 -> -128 Integer.MIN_VALUE
//	1000 0001 -> -127 
//	1000 0010 -> -126 
//	1111 0000 ->  -16 --> 0000 1111
//	
//	1111 0001 ->  -15
//	1111 0010 ->  -14
//	1111 0011 ->  -13 --> 0000 1100 -> 0000 1000 -> 0000 1111 -> 1111 0000
//	1111 0100 ->  -12
//	1111 0101 ->  -11
//	1111 0110 ->  -10 
//	1111 0111 ->   -9 
//	1111 1000 ->   -8 --> 0000 0111
//	1111 1001 ->   -7
//	1111 1010 ->   -6
//	1111 1011 ->   -5
//	1111 1100 ->   -4
//	1111 1101 ->   -3
//	1111 1110 ->   -2
//	1111 1111 ->   -1
//	
//	0000 0000 ->    0
//	0000 0001 ->    1
//	0000 0010 ->    2
//	0000 0011 ->    3
//	0000 0100 ->    4
//	0000 0101 ->    5
//	0000 0110 ->    6
//	0000 0111 ->    7
//	0000 1000 ->    8
//	0000 1001 ->    9
//	0000 1010 ->   10
//	0000 1011 ->   11i.lower & li.lowei.lower & li.lower & lr & l
//	0000 1100 ->   12
//	0000 1101 ->   13 --> 0111 0010
//	0000 1110 ->   14 --> 0111 0001
//	0000 1111 ->   15
//	0001 0000 ->   16
//
//	0111 1100 ->  124
//	0111 1101 ->  125
//	0111 1110 ->  126
//	0111 1111 ->  127 Integer.MAX_VALUE
	
	private AbstractDomain handleShift(AbstractDomain a, ShiftType type){
		// Note: Shifting by x is actually defined as: 
		//    1 << x == 1 << (x & 0xf1)
		// as per: http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.19
		// ref: http://stackoverflow.com/questions/10516786/shifted-by-negative-number-in-java
		// 
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		if(this.upper - this.lower == 0 && i.upper - i.lower == 0){
			//constants
			return moveIntoRange(new Interval(shift(this.lower,i.lower,type)));
		}
		long normalizedLower = i.lower & 0x1f;
		long normalizedRange = Math.min(31, i.upper - i.lower);
		long normalizedUpper = normalizedLower + normalizedRange;
		
		ArrayList<Long> candidates = new ArrayList<Long>();
		long newLower;
		long newUpper;
		candidates.add(shift(this.lower,normalizedLower,type));
		candidates.add(shift(this.upper,normalizedLower,type));
		candidates.add(shift(this.lower,normalizedUpper % 32,type));
		candidates.add(shift(this.upper,normalizedUpper % 32,type));
		if(normalizedUpper > 31){
			//add special cases
			candidates.add(shift(this.lower,0,type));
			candidates.add(shift(this.upper,0,type));
			candidates.add(shift(this.lower,31,type));
			candidates.add(shift(this.upper,31,type));
		} else {
			if(type == ShiftType.USHR && normalizedLower == 0){
				//zero for USHR is really a special case, as it can lead to negative values
				candidates.add(shift(this.lower,0,type));
				candidates.add(shift(this.upper,0,type));
			}
		}
		newLower = Collections.min(candidates);
		newUpper = Collections.max(candidates);
		return moveIntoRange(new Interval(newLower, newUpper));
	}

	public AbstractDomain shl(AbstractDomain a) {
		return handleShift(a, ShiftType.SHL);
	}

	public AbstractDomain shr(AbstractDomain a) {
		return handleShift(a, ShiftType.SHR);
	}
	
	public AbstractDomain ushr(AbstractDomain a) {
		return handleShift(a, ShiftType.USHR);
	}
	
	@Override
	public AbstractDomain join(AbstractDomain a) {
		Interval i = (Interval) a.copy();
		handleOverflow(this); 
		handleOverflow(i);
		if (this.equals(BOT))
			return i.copy();
		if (i.equals(BOT))
			return this.copy();
		return moveIntoRange(new Interval(Math.min(this.lower, i.lower), Math.max(this.upper, i.upper)));
	}
	
	@Override
	public AbstractDomain meet(AbstractDomain a) {
		Interval i = (Interval) a.copy();
		handleOverflow(this); 
		handleOverflow(i);
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
			if (i1.upper - i1.lower == 0 &&
				i2.upper - i2.lower == 0 &&
				i1.equals(i2))
				return new Pair<AbstractDomain, AbstractDomain>(BOT.copy(), BOT.copy());
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
	
	private enum ShiftType {
		SHL, SHR, USHR
	};
	
	private long shift(long a, long b, ShiftType type){
		switch (type) {
		case SHL:
			return a << b;
		case SHR:
			return a >> b;
		case USHR:
			return a >>> b;
		default:
			throw new IllegalArgumentException("type must be != null");
		}
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
		return TOP.copy();
	}
	
//	public AbstractDomain and(AbstractDomain a) {
//	Interval i = (Interval) a;
//	handleOverflow(this); 
//	handleOverflow(i);	
//	if(isTop() || i.isTop()){
//		return TOP.copy();
//	}
//	KillableBitInterval thisKillable = new KillableBitInterval(this);
//	KillableBitInterval iKillable = new KillableBitInterval(i);
//	
//	BigInteger currentBestUpper = PaUtils.bigIntAllZeros();
//	
//	//which bits may be set to 1 on BOTH intervals
//	for(int k = 31; k >= 0; k--){
//		if(thisKillable.mayBeOne(k) && iKillable.mayBeOne(k)){
//			thisKillable.selectOneAtBit(k);
//			iKillable.selectOneAtBit(k);
//
//			// we "select" this bit to be set in both this and i. Then we can discard all other ranges
//			// now assuming that there are no further matches, we can update our currentBestResult to be filled with a one at the matching position
//			currentBestUpper = PaUtils.bigIntSetKthBitToOne(currentBestUpper, k);
//		} else {
//			if(thisKillable.mayBeOne(k)){
//				thisKillable.selectOneOrZeroBit(k);
//			}else{
//				thisKillable.selectZeroAtBit(k);
//			}
//			if(iKillable.mayBeOne(k)){
//				iKillable.selectOneOrZeroBit(k);
//			}else{
//				iKillable.selectZeroAtBit(k);
//			}
//		}
//	}
//	//have upper
//	
//	//reset Killables
//	thisKillable.reset();
//	iKillable.reset();
//	
//	BigInteger currentBestLower = PaUtils.bigIntAllOnes();
//
//	//which bits may be set to 0 on EITHER intervals
//	for(int k = 31; k >= 0; k--){
//		boolean thisMayBeZero = thisKillable.mayBeZero(k);
//		boolean iMayBeZero = iKillable.mayBeZero(k);
//		if(thisMayBeZero || iMayBeZero){
//			if(thisMayBeZero && iMayBeZero) {
//				thisKillable.selectOneOrZeroBit(k);
//				iKillable.selectOneOrZeroBit(k);
//			} else if(thisMayBeZero){
//				thisKillable.selectZeroAtBit(k);
//				iKillable.selectOneOrZeroBit(k);
//			} else if(iMayBeZero){
//				thisKillable.selectOneOrZeroBit(k);
//				iKillable.selectZeroAtBit(k);
//			}
//			currentBestLower = PaUtils.bigIntSetKthBitToZero(currentBestLower, k);
//		}
//	}
//	Interval result = KillableBitInterval.transposeFromUnsignedBigInt(new Pair<BigInteger, BigInteger>(currentBestLower, currentBestUpper));
//	return moveIntoRange(result);
//}
}
