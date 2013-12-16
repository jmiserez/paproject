package ch.ethz.pa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import ch.ethz.pa.domain.AbstractDomain;

public class Analysis extends ForwardBranchedFlowAnalysis<IntervalPerVar> {
	
	public static int WIDENING_ITERATIONS = 5;
	
	ObjectSetPerVar aliases;
	LoopNestTree loops;
	HashMap<Loop,LoopAnnotation> wideningInformation = new HashMap<Loop, LoopAnnotation>();
	
	public Analysis(UnitGraph g, ObjectSetPerVar aliases, LoopNestTree loops) {
		super(g);
		System.err.println(g.toString());
		this.aliases = aliases;
		this.loops = loops;
	}
	
	void run() {
		wideningInformation.clear();
		doAnalysis();
	}
	
	static void unhandled(String what) {
		System.err.println("Can't handle " + what);
		System.exit(1);
	}

	@Override
	protected void flowThrough(IntervalPerVar current, Unit op, List<IntervalPerVar> fallOut,
			List<IntervalPerVar> branchOuts) {
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
		System.err.println("Operation (depth "+currentLoops.size()+"): " + op + "   - " + op.getClass().getName() + "\n      current state: " + current);
				
		if(currentLoops.size() > 0){
			Loop currentInner = currentLoops.get(0);
			if(wideningInformation.get(currentInner) == null){
				wideningInformation.put(currentInner, new LoopAnnotation());
			}
			LoopAnnotation currentAnnotation = wideningInformation.get(currentInner);
			Stmt loopHead = currentInner.getHead(); // the first statement in the loop, usually the condition with goto
			if(s.equals(loopHead)){
				System.err.println("Entering loop");
				if(currentAnnotation.widened){
					// force fixpoint convergence
					// TODO: this is a hack. Really we should be doing this in the merge or somewhere else that is handled by Soot.
					for(String widenedVar : currentAnnotation.widenedValues.getVarNames()){
						fallState.putIntervalForVar(widenedVar, currentAnnotation.widenedValues.getIntervalForVar(widenedVar));
					}
				}
				//we are at the head entering a loop, update count and reset all widenings of nested loops
				currentAnnotation.headCount++;
				currentAnnotation.headStmtValues = fallState.copy();
				
				// all loops < currentInner are nested deeper inside this loop
				for(Loop deeperLoop : loops.headSet(currentInner, false)){
					if(wideningInformation.containsKey(deeperLoop)){
						wideningInformation.put(deeperLoop, new LoopAnnotation()); //completely reset deeper loops
					}
				}
			}
		}
		
		s.apply(new StmtAnalyzer(current, fallState, branchState, aliases));
		
		if(currentLoops.size() > 0){
			Loop currentInner = currentLoops.get(0);
			Stmt loopBackJump = currentInner.getBackJumpStmt();  //the last statement in the loop, not necessarily the goto
			if(s.equals(loopBackJump)){
				System.err.println("Last statement of loop");
				//this was the last instruction in the loop, after this comes the merge() operation
				//need to create a diff between head and backJump
				LoopAnnotation currentAnnotation = wideningInformation.get(currentInner);
				currentAnnotation.backJumpStmtValues = fallState; // as we want the state as changed by the loop body
				
				HashMap<String, Integer> currentDiff = IntervalPerVar.diff(currentAnnotation.headStmtValues, currentAnnotation.backJumpStmtValues);
				currentAnnotation.diffList.add(currentDiff);
				if(currentAnnotation.diffList.size() > WIDENING_ITERATIONS){
					currentAnnotation.diffList.remove(0); //remove first item
				}
				
				//now check if we have had any variable in the diffList that went in the same direction the last WIDENING_ITERATIONS times
				
				boolean wideningNecessary = false;
				
				HashMap<String, Integer> diffCounts = new HashMap<String, Integer>();
				
				HashMap<String, Integer> prevDiff = null;
				for(HashMap<String, Integer> diff : currentAnnotation.diffList){
					for(Entry<String, Integer> entry : diff.entrySet()){
						String varName = entry.getKey();
						Integer direction = entry.getValue();
						
						if(!diffCounts.containsKey(varName)){
							diffCounts.put(varName, 1);
						}
						int count = diffCounts.get(varName);
						if(WIDENING_ITERATIONS == 1 ||(prevDiff != null && prevDiff.get(varName) != null && prevDiff.get(varName).equals(direction))){
							count++;
							diffCounts.put(varName, count); //this variable went in the same direction as previously
							if(count >= WIDENING_ITERATIONS){
								System.err.println(
										 "      fallState: " + fallState + "\n      branchState: " + branchState);
								System.err.println("Doing widening for "+varName);
								
								AbstractDomain interval = fallState.getIntervalForVar(varName);
								AbstractDomain widenedInterval = interval.widen(direction);
								fallState.putIntervalForVar(varName, widenedInterval);
								currentAnnotation.widenedValues.putIntervalForVar(varName, widenedInterval);
								
								diffCounts.put(varName, 0); //reset
								
								System.err.println(
										 "      fallState: " + fallState + "\n      branchState: " + branchState);
								
								wideningNecessary = true;
							}
						}
					}
					prevDiff = diff;
				}
				if(wideningNecessary){
					currentAnnotation.widened = true;
				}
				
			}
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
		
		System.err.println(
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
		if (src1.unreachable) {
			src1 = src1.copy();
			src1.gotoBot();
		}
		if (src2.unreachable) {
			src2 = src2.copy();
			src2.gotoBot();
		}
		IntervalPerVar.join(src1, src2, trg);
		
		System.err.printf("Merge:\n    %s\n    %s\n    ============\n    %s\n",
				src1.toString(), src2.toString(), trg.toString());
	}

	@Override
	protected IntervalPerVar newInitialFlow() {
		return new IntervalPerVar();
	}
}
