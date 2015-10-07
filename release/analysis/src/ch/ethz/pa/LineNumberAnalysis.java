package ch.ethz.pa;

import java.util.HashMap;

import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class LineNumberAnalysis extends ForwardFlowAnalysis<Unit, HashMap<Stmt,Integer>> {

	private int lineNumber = 0;
	private HashMap<Stmt,Integer> lineNumbers = new HashMap<Stmt,Integer>();
	
	public LineNumberAnalysis(DirectedGraph<Unit> g) {
		super(g);
//		System.err.println(g.toString());
	}
	
	HashMap<Stmt,Integer> run() {
		doAnalysis();
		return lineNumbers;
	}
	
	@Override
	protected void flowThrough(HashMap<Stmt,Integer> in, Unit op, HashMap<Stmt,Integer> out) {
		Stmt s = (Stmt)op;
		copy(in, out);
		s.addTag(new PaLineNumberTag(lineNumber++));
		PaLineNumberTag tag = (PaLineNumberTag) s.getTag("PaLineNumber");
		System.err.println(tag.getLineNumber() + ": " + op);
		out.put(s, tag.getLineNumber());
	}

	@Override
	protected HashMap<Stmt,Integer> newInitialFlow() {
		return new HashMap<Stmt,Integer>();
	}

	@Override
	protected HashMap<Stmt,Integer> entryInitialFlow() {
		return new HashMap<Stmt,Integer>();
	}

	@Override
	protected void merge(HashMap<Stmt,Integer> src1, HashMap<Stmt,Integer> src2,
			HashMap<Stmt,Integer> trg) {
		trg.putAll(src1);
		trg.putAll(src2);
	}

	@Override
	protected void copy(HashMap<Stmt,Integer> source, HashMap<Stmt,Integer> dest) {
		//nothing
		dest.clear();
		dest.putAll(source);
	}

}