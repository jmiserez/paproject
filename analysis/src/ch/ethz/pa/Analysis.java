package ch.ethz.pa;

import java.util.List;

import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;


// Implement your numerical analysis here.
public class Analysis extends ForwardBranchedFlowAnalysis<IntervalPerVar> {
	public Analysis(UnitGraph g) {
		super(g);
		System.out.println(g.toString());
	}
	
	void run() {
		//TODO reenable next line
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
		
		System.out.println("Operation: " + op + "   - " + op.getClass().getName() + "\n      current state: " + current
				+ "\n      fallState: " + fallState + "\n      branchState: " + branchState);
		
		s.apply(new StmtAnalyzer(current, fallState, branchState));
		
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
