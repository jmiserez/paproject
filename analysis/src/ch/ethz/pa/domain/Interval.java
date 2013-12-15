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
	
//	/**
//	 * If we can select any two numbers (one from each interval), what is the highest bit that both numbers have set to one?
//	 * E.g. 0000 1010
//	 *      000100110
//	 *      =========
//	 *      000000010
//	 */
//	private int highestCommonOneBit(Interval a, Interval b){
////		int highestA = Integer.highestOneBit(a.upper);
////		int highestB = Integer.highestOneBit(b.upper);
////		
////		for(int i = 31; i >= 0; i--){
////			
////		}
////		
//		return 0; //TODO implement this function
//	}
//	
//	/**
//	 * If we can select any two numbers (one from each interval), what is the lowest bit that at least one number has set to zero?
//	 * E.g. 0000 0010
//	 *      000100100
//	 *      =========
//	 *      000000100
//	 */
//	private int lowestZeroBit(Interval a, Interval b){
////		int highestA = Integer.highestOneBit(a.upper);
////		int highestB = Integer.highestOneBit(b.upper);
//		
//		return 0; //TODO implement this function
//	}
	
//	Interval thisCopy = (Interval) this.copy();
//	Interval iCopy = (Interval) i.copy();
//
////	Basic strategy for &, upper: All bits that can be 1 in exactly both of the ranges
////	- result = 0
////	- while(true){
////		- find highest common highestOneBit() in both ranges
////	 	  -> we do not care about the less significant bits, as they will have less of an effect
////		- if there is such a bit
////			- set the bit on the result
////			- discard all more significant bits and the bit itself in both ranges
////		- else
////			- return result
//	newUpper = 0;
//	int highestOneBit = 0;
//	while((highestOneBit = highestCommonOneBit(thisCopy, iCopy)) != 0){
//		newUpper |= highestOneBit;
//		thisCopy = killGeqBits(thisCopy, highestOneBit);
//		iCopy = killGeqBits(iCopy, highestOneBit);
//	}
//	candidates.add(newUpper);
//	
//	thisCopy = (Interval) this.copy();
//	iCopy = (Interval) i.copy();
//	
//	 /*     0000 0010
//	 *      0001 0100
//	 *      =========
//	 *      0000 0100
//	 *      ---------
//	 *      0000 0010
//	 *      0000 0000
//	 *      =========
//	 */
//	
////	Basic strategy for &, lower: All bits that can be 0 in at least one of the ranges
//	newLower = 0;
//	int lowestZeroBit = -1; // ones
//	int prevZeroBit;
//	do {
//		prevZeroBit = lowestZeroBit;
//		lowestZeroBit = lowestZeroBit(thisCopy, iCopy);
//		newLower &= ~lowestZeroBit; //set to 0
//		thisCopy = killGeqBits(thisCopy, lowestZeroBit);
//		iCopy = killGeqBits(iCopy, lowestZeroBit);
//	} while(prevZeroBit != lowestZeroBit);
//	candidates.add(newLower);
	
//	Interval i = (Interval) a;
//
//	List<Integer> newBounds = new ArrayList<Integer>();
//	long[] thisBounds = {this.lower, this.upper};
//	long[] iBounds = {i.lower, i.upper};
//
//	// 0: -/- -> intersection = [X, -1] (X is calculated as seen below)
//	// 1: -/+ -> intersection = [0,0]
//	// 2: +/- -> intersection = [0,0]
//	// 3: +/+ -> intersection = [0, Y] (Y is calculated as seen below)
//	
//	for(long tBound : thisBounds){
//		for(long iBound : iBounds){
//			if(tBound < 0 && iBound < 0){
//				newBounds.add(~((1 << (Integer.numberOfLeadingZeros(Math.max(~tBound, ~iBound)))+1) - 1));
//				newBounds.add(-1);
//			}
//			// case 2: this negative, i positive
//			if(tBound < 0 && iBound >= 0){
//				//TODO
//			}
//			// case 3: this positive, i negative
//			if(tBound < 0 && iBound >= 0){
//				//TODO
//			}
//			// case 4: both positive. AND values of intersection go up from 0 to this value
//			if(tBound < 0 && iBound < 0){
//				newBounds.add(0);
//				// 0000 0111
//				// 0001 0000
//				// 0001 0000
//				newBounds.add((1 << (Integer.numberOfLeadingZeros(Math.max(tBound, iBound)) + 1))
//						+ ((1 << (Integer.numberOfLeadingZeros(Math.min(tBound, iBound)) + 1)) - 1));
//				
//			}
//		}
//	}
//	if(newBounds.size() < 1){
//		return TOP;
//	}
//	return handleOverflow(new Interval(Collections.min(newBounds), Collections.max(newBounds)));

	
//	1000 0000 -> -128 Integer.MIN_VALUE
//	1000 0001 -> -127 
//	1000 0010 -> -126 
//	1111 0000 ->  -16
//	
//	1111 0001 ->  -15
//	1111 0010 ->  -14
//	1111 0011 ->  -13
//	1111 0100 ->  -12
//	1111 0101 ->  -11
//	1111 0110 ->  -10
//	1111 0111 ->   -9
//	1111 1000 ->   -8
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
//	0000 1011 ->   11
//	0000 1100 ->   12
//	0000 1101 ->   13
//	0000 1110 ->   14
//	0000 1111 ->   15
//	0001 0000 ->   16
//
//	0111 1100 ->  124
//	0111 1101 ->  125
//	0111 1110 ->  126
//	0111 1111 ->  127 Integer.MAX_VALUE
	
// Examples:
//	[3,7] & [7,11]
//	[3,7] | [7,11]
//	0000 0011 ->    3
//	0000 0100 ->    4
//	0000 0101 ->    5
//	0000 0110 ->    6
//	0000 0111 ->    7
//	
//	0000 0111 ->    7
//	0000 1000 ->    8
//	0000 1001 ->    9
//	0000 1010 ->   10
//	0000 1011 ->   11
//	
//	& lower: 3 & 8 = 0
//	0000 0000
//	& upper: 7 & 7 = 7
//	0000 0111
//	| lower: 3 | 7 = 7
//	0000 0111
//	| upper: 7 | 11 = 15
//	0000 1111
//	

//	0000 0011 ->    3  
//	0000 0100 ->    4
//	
//	0000 0101 ->    5
//	0000 0110 ->    6
//	
//  & : [1,4]
	
	
	/**
	 * Split an interval along the log2(k)-th bit (LSB is bit 1).
	 * 
	 * Returns a Pair of Intervals, the first being [l, k-1] and the second [k, u].
	 * 
	 * E.g. A = [3,4] == [0011, 0100]
	 *   
	 *      split(A, 3) = {[3], [4]}
	 *      
	 * Note: This is similar to the KillBit function defined for the Bitwise domain, but more precise.  
	 */
	protected Pair<Interval, Interval> halve(int k){
		Interval first = null;
		if(this.lower <= k-1){
			first = new Interval(this.lower, k-1);
		}
		Interval second = null;
		if(k <= this.upper){
			second = new Interval(k, this.upper);
		}
		return new Pair<Interval, Interval>(first, second);
	}
	protected Pair<Interval, Interval> halveBit(int k){
		return halve(1 << k);
	}
	protected Interval asUnsigned(){
		//conversion to unsigned long
		long newLower = this.lower & 0xffffffff;
		long newUpper = this.upper & 0xffffffff;
		
		if(newLower > newUpper){
			//swap values
			newLower = newLower ^ newUpper;
			newUpper = newLower ^ newUpper;
			newLower = newLower ^ newUpper;
		}
		
		return new Interval(newLower, newUpper);
	}
	
	public boolean containsValue(long v) {
		return (v >= lower && v <= upper);
	}

	/**
	 * Returns an array of k 32-1 intervals. Each interval represents the interval KillBit_int(v,k), where v is the the subinterval of this where the k-th bit is set to 1.
	 * 
	 * KillBit_int definition: Regehr, Duongsaa: "Deriving Abstract Transfer Functions for Analyzing Embedded Software"
	 *                           url: http://www.cs.utah.edu/~regehr/papers/lctes06_2/fp019-regehr.pdf
	 */
	protected ArrayList<Interval> split(){
		ArrayList<Interval> result = new ArrayList<Interval>(32);
		//all unsigned operations, assume unsigned
		for(int k = 0; k < 32; k++){
			Interval partial = null;
			long cursor = 1 << k;
			if(this.containsValue(cursor)){
				//cursor is start value, end is the largest value <= (1 << k) - 1
				long start = cursor;
				long end = cursor;
				if(this.upper >= cursor-1){
					end = cursor-1;
				}else{
					end = this.upper;
				}
				// all values between start and end have the k-th bit set. Thus:
				start &= (1 << k);
				end &= (1 << k);
				//start is still guaranteed to be <= end
				partial = new Interval(start, end);
			}
			result.add(k, partial);	
		}
		return result;
	}
	
	
	public AbstractDomain and(AbstractDomain a) {
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
//		ArrayList<Integer> candidates = new ArrayList<Integer>();
//		int newLower;
//		int newUpper;
//		
////		0000 0011 ->    3  
////		0000 0100 ->    4
////	
////		0000 0101 ->    5
////		0000 0110 ->    6
////	
////  & : [1,4]
//		
////		0000 0011 ->    3  
////		0000 0100 ->    4
////	
////		0000 1001 ->    9
////		0000 1010 ->   10
//		
//		ArrayList<Interval> thisIntervals = new ArrayList<Interval>();
//		ArrayList<Interval> iIntervals = new ArrayList<Interval>();
//		
//		// UPPER
//		// the maximum number of bits that can be set to one
//		
//		int thisHighestOneBit = Integer.highestOneBit(thisUpper);
//		int iHighestOneBit = Integer.highestOneBit(iUpper);
//		
//		
//		
//		
//		int highestCommonOneBit = Math.min(Integer.highestOneBit((int) this.upper), Integer.highestOneBit((int) i.upper));
//		
//		
//		
//		newLower = Collections.min(candidates);
//		newUpper = Collections.max(candidates);
//		return handleOverflow(new Interval(newLower, newUpper));
		return TOP.copy();

	}

	public AbstractDomain or(AbstractDomain a) {
		return TOP.copy();

	}

	public AbstractDomain xor(AbstractDomain a) {
		return TOP.copy(); //TODO implement
	}
	
	private AbstractDomain handleShift(AbstractDomain a, ShiftType type){
		// Note: Shifting by x is actually defined as: 
		//    1 << x == 1 << (x & 0xf1)
		// as per: http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.19
		// ref: http://stackoverflow.com/questions/10516786/shifted-by-negative-number-in-java
		// 
		Interval i = (Interval) a;
		if(isTop() || i.isTop()){
			return TOP.copy();
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
		return handleOverflow(new Interval(newLower, newUpper));
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
		Interval result = (Interval) this.copy();
		return TOP.copy();
	}

}
