package ch.ethz.pa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LoopAnnotation {
	int headCount = 0;
	boolean widened = false;
	
	IntervalPerVar headStmtValues = new IntervalPerVar(); //values at the start of the loop
	IntervalPerVar backJumpStmtValues = new IntervalPerVar(); //values at the end of the loop
	
	IntervalPerVar widenedValues = new IntervalPerVar();

	// we will apply widening as soon as any of these reach WIDENING_ITERATIONS changes in the same direction.
	List<HashMap<String, Integer>> diffList = new LinkedList<HashMap<String, Integer>>();
}
