package ch.ethz.pa.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import soot.toolkits.scalar.Pair;

public class KillableBitInterval {
	
	public static boolean BITWISE_SLOW_BUT_MAXIMALLY_PRECISE = false;
	public static int BITWISE_PRECISENESS = 4; //higher is better but slower
	
	private Pair<BigInteger, BigInteger> initialValue;
	private HashMap<Integer, ArrayList<Pair<BigInteger, BigInteger>>> splitList = new HashMap<Integer, ArrayList<Pair<BigInteger,BigInteger>>>();
	
	public KillableBitInterval(Pair<BigInteger, BigInteger> initial) {
		this.initialValue = initial;
		reset();
	}
	
	public KillableBitInterval(Interval fromInterval) {
		this.initialValue = transposeToUnsignedBigInt(fromInterval);
		reset();
	}
	
	/**
	 * reset to starting value
	 */
	public void reset(){
		splitList.clear();
		for(int k = 0; k < 32; k++){
			splitList.put(k, new ArrayList<Pair<BigInteger, BigInteger>>());
		}
		HashMap<Integer, Pair<BigInteger, BigInteger>> split = split(initialValue);
		for(int k = 0; k < 32; k++){
			splitList.get(k).add(split.get(k));
		}
	}
	
	/**
	 * Continue as if the k-th bit was fixed to be 1
	 */
	public void selectOneAtBit(int k){
		if(nonNullEntries(splitList.get(k)).size() > 0){
			ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(splitList.get(k));
			splitList.clear(); //discard THIS ONE and AND ALL OTHER ZEROS, we want to just use THIS ONE
			killBitSubrange(killBitCandidates, k, splitList);//re-add lower bits of THIS ONE
		}
	}
	
	/**
	 * Continue as if the k-th bit could be either 0 or 1
	 * @param k
	 */
	public void selectOneOrZeroBit(int k){
		if(nonNullEntries(splitList.get(k)).size() > 0){
			ArrayList<Pair<BigInteger, BigInteger>> killBitCandidates = new ArrayList<Pair<BigInteger,BigInteger>>(splitList.get(k));
			splitList.get(k).removeAll(nonNullEntries(killBitCandidates)); //discard THIS ONE, we want THIS ONE or ANY OTHER of the ZEROS
			killBitSubrange(killBitCandidates, k, splitList); //re-add lower bits of THIS ONE
		}
	}
	
	/**
	 * Continue as if the k-th bit was fixed to be 0
	 */
	public void selectZeroAtBit(int k){
		splitList.get(k).clear(); //discard ALL ONES
	}
	
	/**
	 * It is possible for the k-th bit to be 1
	 */
	public boolean mayBeOne(int k){
		return nonNullEntries(splitList.get(k)).size() > 0;
	}
	
	/**
	 * It is possible for the k-th bit to be 0. 
	 * 
	 * The idea here is that either:
	 *   - The splitList[k] has no entries, or
	 *   - There are already entries in the splitList[x], where x < k.
	 *     -> Any of these entries implies that x is the highest bit set to one, therefore
	 *        the current bit may be 0.
	 */
	public boolean mayBeZero(int k){
		boolean splitListHasEntriesForSmallerK = false;
		if(k > 0){
			for(int m = k-1; m >= 0; m--){
				if(nonNullEntries(splitList.get(m)).size() != 0){
					splitListHasEntriesForSmallerK = true;
					break;
				}
			}
		}
		return splitListHasEntriesForSmallerK || nonNullEntries(splitList.get(k)).size() == 0;
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
	protected static Pair<BigInteger, BigInteger> transposeToUnsignedBigInt(Interval a){
		//transpose the Interval by Integer.MIN_VALUE
		
		BigInteger newLower = new BigInteger(String.valueOf(a.lower)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());
		BigInteger newUpper = new BigInteger(String.valueOf(a.upper)).add(new BigInteger(String.valueOf(Integer.MIN_VALUE)).negate());
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
								BigInteger l = elem.getO1();
								BigInteger u = elem.getO2();
//								BigInteger l = new BigInteger("1").shiftLeft(elem.getO1().bitLength()-1);
//								BigInteger u = new BigInteger("1").shiftLeft(elem.getO2().bitLength()).subtract(new BigInteger("1"));
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

}
