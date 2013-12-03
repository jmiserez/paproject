package ch.ethz.pa;

import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Flow-insensitive pointer analysis
 */
public class PointsToAnalysis extends ForwardFlowAnalysis<Unit, ObjectSetPerVar> {

	ObjectSetPerVar lastOut;
	
	public PointsToAnalysis(DirectedGraph<Unit> g) {
		super(g);
		System.out.println(g.toString());
		lastOut = null;
	}
	
	ObjectSetPerVar run() {
		doAnalysis();
		return lastOut;
	}
	
	static void unhandled(String what) {
		System.err.println("Can't handle " + what);
		System.exit(1);
	}
	
	@Override
	protected void flowThrough(ObjectSetPerVar in, Unit op, ObjectSetPerVar out) {
		Stmt s = (Stmt)op;
		out.copyFrom(in);
		
		System.out.println("Operation (pointer analysis): " + op + "   - " + op.getClass().getName() + "\n      in: " + in
				+ "\n      out: " + out);
		
		s.apply(new PointsToStmtAnalyzer(in, out));
		lastOut = out;
	}

	@Override
	protected ObjectSetPerVar newInitialFlow() {
		return new ObjectSetPerVar();
	}

	@Override
	protected ObjectSetPerVar entryInitialFlow() {
		// TODO: How do you model the entry point?
		return new ObjectSetPerVar();
	}

	@Override
	protected void merge(ObjectSetPerVar src1, ObjectSetPerVar src2,
			ObjectSetPerVar trg) {
		ObjectSetPerVar.join(src1, src2, trg);
		System.out.printf("Merge (pointer analysis):\n    %s\n    %s\n    ============\n    %s\n",
				src1.toString(), src2.toString(), trg.toString());
	}

	@Override
	protected void copy(ObjectSetPerVar source, ObjectSetPerVar dest) {
		dest.copyFrom(source);
	}

}
