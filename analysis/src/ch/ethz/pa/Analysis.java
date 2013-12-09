package ch.ethz.pa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

public class Analysis extends ForwardBranchedFlowAnalysis<IntervalPerVar> {
	
	private final static int WIDENING_ITERATIONS = 5;
	
	ObjectSetPerVar aliases;
	LoopNestTree loops;
	TreeMap<Loop,Integer> loopCounts = new TreeMap<Loop, Integer>();
	List<Loop> widenedLoops = new ArrayList<Loop>();
	HashMap<String, Integer> loopChanges = new HashMap<String,Integer>();
	
	public Analysis(UnitGraph g, ObjectSetPerVar aliases, LoopNestTree loops) {
		super(g);
		System.out.println(g.toString());
		this.aliases = aliases;
		this.loops = loops;
	}
	
	void run() {
		//TODO reenable next line
		loopCounts.clear();
		widenedLoops.clear();
		loopChanges.clear();
		doAnalysis();
	}
	
	static void unhandled(String what) {
		System.err.println("Can't handle " + what);
		System.exit(1);
	}

	@Override
	protected void flowThrough(IntervalPerVar current, Unit op, List<IntervalPerVar> fallOut,
			List<IntervalPerVar> branchOuts) {
		// TODO: This can be optimized.
	
		Stmt s = (Stmt)op;
		IntervalPerVar fallState = new IntervalPerVar();
		fallState.copyFrom(current);
		IntervalPerVar branchState = new IntervalPerVar();
		branchState.copyFrom(current);
		
		List<Loop> currentLoops = new ArrayList<Loop>();
		//The LoopNestTree iterator will always return the innermost loops first
		for(Loop l : loops){
			if(l.getLoopStatements().contains(s)){
				currentLoops.add(l);
			}
		}
		//currentLoops.size() is the current nesting depth
		
		System.out.println("Operation (depth "+currentLoops.size()+"): " + op + "   - " + op.getClass().getName() + "\n      current state: " + current);
		
		if(currentLoops.size() > 0){
			Loop currentInner = currentLoops.get(0);
			Stmt loopHead = currentInner.getHead();
			if(s.equals(loopHead)){
				//we are at the head, update count
				int loopCount = 0;
				if(loopCounts.containsKey(currentInner)){
					loopCount = loopCounts.get(currentInner);
					loopCounts.put(currentInner, loopCount++);
				} else {
					//first time we enter this loop, we must be at loopHead
					if(!loopHead.equals(s)){
						System.err.println("Warning: did not enter loop through loop head!");
					}
					loopCounts.put(currentLoops.get(0), 1);
				}
				
			}
			
		}
		
		boolean skip = false;
		for(Loop l : currentLoops){
			if(widenedLoops.contains(l)){
				skip = true;
				break;
			}
		}
		if(!skip){
			s.apply(new StmtAnalyzer(current, fallState, branchState, aliases));
		}
		
		// TODO: Maybe avoid copying objects too much. Feel free to optimize.
		for (IntervalPerVar fnext : fallOut) {
			if (fallState != null) {
				fnext.copyFrom(fallState);
			}
		}
		for (IntervalPerVar fnext : branchOuts) {
			if (branchState != null) {
				fnext.copyFrom(branchState);
			}
		}		
		
		System.out.println(
				 "      fallState: " + fallState + "\n      branchState: " + branchState);
	}

	@Override
	protected void copy(IntervalPerVar source, IntervalPerVar dest) {
		dest.copyFrom(source);
	}

	@Override
	protected IntervalPerVar entryInitialFlow() {
		// TODO: How do you model the entry point?
		return new IntervalPerVar();
	}

	@Override
	protected void merge(IntervalPerVar src1, IntervalPerVar src2, IntervalPerVar trg) {
		// TODO: join, widening, etc goes here.
		IntervalPerVar.join(src1, src2, trg);
		System.out.printf("Merge:\n    %s\n    %s\n    ============\n    %s\n",
				src1.toString(), src2.toString(), trg.toString());
	}

	@Override
	protected IntervalPerVar newInitialFlow() {
		return new IntervalPerVar();
	}
}
