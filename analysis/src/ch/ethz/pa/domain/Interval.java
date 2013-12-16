package ch.ethz.pa.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import soot.jimple.ConditionExpr;
import soot.toolkits.scalar.Pair;

/* This is default on purpose, use Domain for creating objects
 * or AbstractDomain as type for variables.
 */
class Interval extends AbstractDomain {
	
	public static boolean BITWISE_SLOW_BUT_MAXIMALLY_PRECISE = false;
	public static int BITWISE_PRECISENESS = 8; //higher is better but slower
	
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
		return moveIntoRange(new Interval(newLower, newUpper));
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
	
	
	/**
	 * Transpose the interval to a completely signed range by adding Integer.MIN_VALUE
	 * 
	 * This allows us to treat it as an unsigned long as far as all bit operations are concerned and transpose it back once we've done the bitwise operations.
	 * 
	 * The old range: [0xffff ffff 8000 0000, 0x0000 0000 7fff ffff] (64bit signed int)
	 * The new range: [0x0000 0000 0000 0000, 0xffff ffff ffff ffff] (65+bit signed BigInteger)
	 * 
	 * @return
	 */
	protected Pair<BigInteger, BigInteger> transposeToUnsignedBigInt(){
		//transpose the Interval by Integer.MIN_VALUE
		
		BigInteger newLower = new BigInteger(String.valueOf(this.lower)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());
		BigInteger newUpper = new BigInteger(String.valueOf(this.upper)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());
		return new Pair<BigInteger, BigInteger>(newLower, newUpper);
	}
	
	protected static Interval transposeFromUnsignedBigInt(Pair<BigInteger, BigInteger> bigInterval){
		//transpose the Interval by Integer.MIN_VALUE
		
		long newLower = bigInterval.getO1().subtract(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate()).longValue();
		long newUpper = bigInterval.getO2().subtract(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate()).longValue();
		
		return new Interval(newLower, newUpper);
	}

	/**
	 * Returns an array of k 32-1 big intervals. Each big interval represents the interval KillBit_int(v,k), where v is the the subinterval of this where the k-th bit is set to 1.
	 * 
	 * KillBit_int definition: Regehr, Duongsaa: "Deriving Abstract Transfer Functions for Analyzing Embedded Software"
	 *                           url: http://www.cs.utah.edu/~regehr/papers/lctes06_2/fp019-regehr.pdf
	 */
	protected static HashMap<Integer, Pair<BigInteger, BigInteger>> split(Pair<BigInteger, BigInteger> bigInterval){
		BigInteger bigLower = bigInterval.getO1();
		BigInteger bigUpper = bigInterval.getO2();
		HashMap<Integer, Pair<BigInteger, BigInteger>> result = new HashMap<Integer, Pair<BigInteger, BigInteger>>(32);
		//all unsigned operations, assume unsigned
		for(int k = 31; k >= 0; k--){
			Pair<BigInteger, BigInteger> partial = null;
			
			//
			//  0000 0000
			//  0000 1000
			//  0010 0000
			//  0010 1100
			//  0011 1111
			//
			
			BigInteger cursorStart = new BigInteger("1").shiftLeft(k);
			BigInteger cursorEnd = new BigInteger("1").shiftLeft(k+1).subtract(new BigInteger("1"));
			//are there any numbers in this range
			
			//         [               ]
			//      |                     |
			//      |        |
			//            |          |
			//                  |            |
			//
			if(bigLower.compareTo(cursorStart) <= 0 && bigUpper.compareTo(cursorEnd) >= 0){
				partial = new Pair<BigInteger, BigInteger>(cursorStart, cursorEnd);
			} else if(bigLower.compareTo(cursorStart) <= 0 && bigUpper.compareTo(cursorStart) >= 0 && bigUpper.compareTo(cursorEnd) <= 0 ){
				partial = new Pair<BigInteger, BigInteger>(cursorStart, bigUpper);
			} else if(bigLower.compareTo(cursorStart) >= 0 && bigLower.compareTo(cursorEnd) <= 0 && bigUpper.compareTo(cursorStart) >= 0 && bigUpper.compareTo(cursorEnd) <= 0 ){
				partial = new Pair<BigInteger, BigInteger>(bigLower, bigUpper);
			} else if(bigLower.compareTo(cursorStart) >= 0 && bigLower.compareTo(cursorEnd) <= 0 && bigUpper.compareTo(cursorEnd) >= 0){
				partial = new Pair<BigInteger, BigInteger>(bigLower, cursorEnd);
			}
			if(partial != null){
				BigInteger p1 = partial.getO1().and(new BigInteger("1").shiftLeft(k).not());
				BigInteger p2 = partial.getO2().and(new BigInteger("1").shiftLeft(k).not());
				partial.setO1(p1);
				partial.setO2(p2);
			}
			result.put(k, partial);	
		}
		return result;
	}
	
	private static <T> ArrayList<T> nonNullEntries(List<T> list){
		ArrayList<T> result = new ArrayList<T>();
		for(T elem : list){
			if(elem != null){
				result.add(elem);
			}
		}
		return result;
	}
	
	/**
	 * This is the actual KillBit_int(v,k) function. This function assumes that the MSB bit of both lower and upper is k, thus the range is bounded by this bit k.
	 */
	private static void killBitSubrange(ArrayList<Pair<BigInteger, BigInteger>> killBitRanges, int k , HashMap<Integer, ArrayList<Pair<BigInteger, BigInteger>>> splitListToAddTo){
		ArrayList<Pair<BigInteger, BigInteger>> cloneList = new ArrayList<Pair<BigInteger,BigInteger>>(killBitRanges);
		for(Pair<BigInteger, BigInteger> elem : cloneList){
			if(elem != null){
				BigInteger elemLower = elem.getO1().and(new BigInteger("1").shiftLeft(k).not()); //unset the k-th bit
				BigInteger elemUpper = elem.getO2().and(new BigInteger("1").shiftLeft(k).not()); //unset the k-th bit
				
				HashMap<Integer, Pair<BigInteger, BigInteger>> currentSplit = split(new Pair<BigInteger, BigInteger>(elemLower, elemUpper));
				for(int m = 0; m < 32; m++){
					if(splitListToAddTo.get(m) == null){
						splitListToAddTo.put(m, new ArrayList<Pair<BigInteger, BigInteger>>());
					}
					if(currentSplit.get(m) != null){
						splitListToAddTo.get(m).add(currentSplit.get(m));
					}
					splitListToAddTo.get(m).removeAll(Collections.singleton(null)); //remove all nulls
				}
			}
		}
		if(!BITWISE_SLOW_BUT_MAXIMALLY_PRECISE){
			// need to reduce the number of splits in the list. Otherwise our runtime is O(n!), where n is the number of bits in an integer.
			//
			// Here we just fudge the results and merge all ranges together. E.g.:
			//			
			// [0000,0000], [0100,0101] -> would result in [0000,0101], although that is clearly not precise.
			//			
			for(int m = 0; m < 32; m++){
				// note that this adds impreciseness as described in the paper.
				ArrayList<Pair<BigInteger, BigInteger>> listToMerge = splitListToAddTo.get(m);
				Pair<BigInteger, BigInteger> newBounds = null;
				if(listToMerge.size() > BITWISE_PRECISENESS){
					newBounds = null;
					for(Pair<BigInteger, BigInteger> elem : listToMerge){
						if(elem != null){
							if(newBounds == null){
								newBounds = new Pair<BigInteger, BigInteger>(elem.getO1(), elem.getO2());
							} else {
								BigInteger l = new BigInteger("1").shiftLeft(elem.getO1().bitLength()-1);
								BigInteger u = new BigInteger("1").shiftLeft(elem.getO2().bitLength()).subtract(new BigInteger("1"));
								if(l.compareTo(newBounds.getO1()) < 0){
									newBounds.setO1(l);
								}
								if(u.compareTo(newBounds.getO2()) > 0){
									newBounds.setO2(u);
								}
							}
						}
					}
					ArrayList<Pair<BigInteger, BigInteger>> newList = new ArrayList<Pair<BigInteger, BigInteger>>();
					newList.add(newBounds);
					splitListToAddTo.put(m, newList);
				}
			}
		}
	}
	
	public AbstractDomain and(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);	
		if(isTop() || i.isTop()){
			return TOP.copy();
		}

		
		Pair<BigInteger, BigInteger> thisBig = this.transposeToUnsignedBigInt();
		Pair<BigInteger, BigInteger> iBig = i.transposeToUnsignedBigInt();
		
		// lists containing all splits, e.g.
		// 0000 1100 -> we'd get an interval starting from 1100
		// 0100 1000 -> we'd get an interval starting from 1000, also with same length
		//
		HashMap<Integer, ArrayList<Pair<BigInteger, BigInteger>>> thisSplitList = new HashMap<Integer, ArrayList<Pair<BigInteger,BigInteger>>>();
		HashMap<Integer, ArrayList<Pair<BigInteger, BigInteger>>> iSplitList = new HashMap<Integer, ArrayList<Pair<BigInteger,BigInteger>>>();
		for(int k = 0; k < 32; k++){
			thisSplitList.put(k, new ArrayList<Pair<BigInteger, BigInteger>>());
			iSplitList.put(k, new ArrayList<Pair<BigInteger, BigInteger>>());
		}
		
		HashMap<Integer, Pair<BigInteger, BigInteger>> thisSplit = split(thisBig);
		HashMap<Integer, Pair<BigInteger, BigInteger>> iSplit = split(iBig);
		
		for(int k = 0; k < 32; k++){
			thisSplitList.get(k).add(thisSplit.get(k));
			iSplitList.get(k).add(iSplit.get(k));
		}
		//now these lists contain just one split
		
		BigInteger currentBestUpper = new BigInteger("0");
		
		//which bits may be set to 1 on BOTH intervals
		for(int k = 31; k >= 0; k--){
			int highestCommonOneBit = -1;
			if(nonNullEntries(thisSplitList.get(k)).size() > 0 && nonNullEntries(iSplitList.get(k)).size() > 0){
				highestCommonOneBit = k;
			}
			if(nonNullEntries(thisSplitList.get(k)).size() > 0){
				//we need to kill the bit in each of the splits and add the remainders to the splits list
				ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(thisSplitList.get(k));
				thisSplitList.clear();
				killBitSubrange(killBitCandidates, k, thisSplitList);
			}
			if(nonNullEntries(iSplitList.get(k)).size() > 0){
				ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(iSplitList.get(k));
				iSplitList.clear();
				killBitSubrange(killBitCandidates, k, iSplitList);

			}
			if(highestCommonOneBit >= 0){
				// we "select" this bit to be set in both this and i. Then we can discard all other ranges
				// now assuming that there are no further matches, we can update our currentBestResult to be filled with a one at the matching position
				
				currentBestUpper = currentBestUpper.or(new BigInteger("1").shiftLeft(highestCommonOneBit));
			}
		}
		//have upper
		
		//reset lists
		thisSplitList.clear();
		iSplitList.clear();
		for(int k = 0; k < 32; k++){
			thisSplitList.put(k, new ArrayList<Pair<BigInteger, BigInteger>>());
			iSplitList.put(k, new ArrayList<Pair<BigInteger, BigInteger>>());
		}
		for(int k = 0; k < 32; k++){
			thisSplitList.get(k).add(thisSplit.get(k));
			iSplitList.get(k).add(iSplit.get(k));
		}
		
		BigInteger currentBestLower = new BigInteger(String.valueOf(Integer.MAX_VALUE)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());

		//which bits may be set to 0 on EITHER interval
		for(int k = 31; k >= 0; k--){
			int highestZeroBit = -1; //00 is first bit
			
			boolean splitListhasEntriesForSmallerK = false;
			if(k > 0){
				for(int m = k-1; m >= 0; m--){
					if(nonNullEntries(thisSplitList.get(m)).size() != 0 || nonNullEntries(iSplitList.get(m)).size() != 0){
						splitListhasEntriesForSmallerK = true;
						break;
					}
				}
			}
			
			if(splitListhasEntriesForSmallerK || nonNullEntries(thisSplitList.get(k)).size() == 0 || nonNullEntries(iSplitList.get(k)).size() == 0){
				highestZeroBit = k; //there is a zero here
				//discard all ones
				thisSplitList.get(k).clear();
				iSplitList.get(k).clear();
			} else {
				//killBit
				if(nonNullEntries(thisSplitList.get(k)).size() > 0){
					ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(thisSplitList.get(k));
					thisSplitList.get(k).removeAll(nonNullEntries(killBitCandidates));
					killBitSubrange(killBitCandidates, k, thisSplitList);
				}
				if(nonNullEntries(iSplitList.get(k)).size() > 0){
					ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(iSplitList.get(k));
					iSplitList.get(k).removeAll(nonNullEntries(killBitCandidates));
					killBitSubrange(killBitCandidates, k, iSplitList);
				}
			}
			if(highestZeroBit >= 0){
				// we "select" this bit to be not set in both this and i. Then we can discard all other ranges
				// now assuming that there are no further matches, we can update our currentBestResult to be filled with a zero at the matching position
				
				currentBestLower = currentBestLower.and(new BigInteger("1").shiftLeft(highestZeroBit).not());
			}
		}
		
		Interval result = transposeFromUnsignedBigInt(new Pair<BigInteger, BigInteger>(currentBestLower, currentBestUpper));
		return moveIntoRange(result);
	}

	public AbstractDomain or(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		return TOP.copy(); //TODO implement

	}

	public AbstractDomain xor(AbstractDomain a) {
		Interval i = (Interval) a;
		handleOverflow(this); 
		handleOverflow(i);
		if(isTop() || i.isTop()){
			return TOP.copy();
		}
		return TOP.copy(); //TODO implement
	}
	
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


}
