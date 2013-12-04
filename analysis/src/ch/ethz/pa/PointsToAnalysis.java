package ch.ethz.pa;

import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Flow-insensitive pointer analysis
 */
public class PointsToAnalysis extends ForwardFlowAnalysis<Unit, ObjectSetPerVar> {

	ObjectSetPerVar lastOut;
	LoopNestTree loops;
//	HashMap<Loo/p, Integer> loopsVisited = new HashMap<Loop, Integer>();
	
	public PointsToAnalysis(DirectedGraph<Unit> g, LoopNestTree loops) {
		super(g);
		System.out.println(g.toString());
		lastOut = null;
//		this.loops = loops;
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

//		int count = 0;
//		for(Loop l : loops){
//			if(l.getHead().equals(s)){
//				if(!loopsVisited.containsKey(l)){
//					loopsVisited.put(l,0);
//					count = 1;
//				}else {
//					count = loopsVisited.get(l);
//					loopsVisited.put(l, count++);
//				}
//				System.out.println("Operation (pointer analysis): " + op + "   - " + op.getClass().getName() + "\n      in: " + in
//						+ "\n      out: " + out);
//			}
//		}
		
		out.copyFrom(in);
		s.apply(new PointsToStmtAnalyzer(in, out));
		System.out.println("Operation (pointer analysis): " + op + "   - " + op.getClass().getName() + "\n      in: " + in
				+ "\n      out: "+out);
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
