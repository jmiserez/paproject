package ch.ethz.pa;

import java.util.List;

import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;


// Implement your numerical analysis here.
public class Analysis extends ForwardBranchedFlowAnalysis<IntervalPerVar> {
	public Analysis(UnitGraph g) {
		super(g);
		//System.out.println(g.toString());
	}
	
	void run() {
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
		//System.out.println("Operation: " + op + "   - " + op.getClass().getName() + "\n      state: " + current);
	
		Stmt s = (Stmt)op;
		IntervalPerVar fallState = new IntervalPerVar();
		fallState.copyFrom(current);
		IntervalPerVar branchState = new IntervalPerVar();
		branchState.copyFrom(current);
		
		if (s instanceof DefinitionStmt) {
			DefinitionStmt sd = (DefinitionStmt) s;
			Value left = sd.getLeftOp();
			Value right = sd.getRightOp();
			//System.out.println(left.getClass().getName() + " " + right.getClass().getName());
			
			// You do not need to handle these cases:
			if ((!(left instanceof StaticFieldRef))
					&& (!(left instanceof JimpleLocal))
					&& (!(left instanceof JArrayRef))
					&& (!(left instanceof JInstanceFieldRef)))
				unhandled("1: Assignment to non-variables is not handled.");
			else if ((left instanceof JArrayRef)
					&& (!((((JArrayRef) left).getBase()) instanceof JimpleLocal)))
				unhandled("2: Assignment to a non-local array variable is not handled.");

			// TODO: Handle other cases. For example:

			if (left instanceof JimpleLocal) {
				String varName = ((JimpleLocal)left).getName();				
				
				if (right instanceof IntConstant) {
					IntConstant c = ((IntConstant)right);
					fallState.putIntervalForVar(varName, new Interval(c.value, c.value));
				} else if (right instanceof JimpleLocal) {
					JimpleLocal l = ((JimpleLocal)right);
					fallState.putIntervalForVar(varName, current.getIntervalForVar(l.getName()));
				} else if (right instanceof BinopExpr) {
					Value r1 = ((BinopExpr) right).getOp1();
					Value r2 = ((BinopExpr) right).getOp2();
					
					Interval i1 = tryGetIntervalForValue(current, r1);
					Interval i2 = tryGetIntervalForValue(current, r2);
					
					if (i1 != null && i2 != null) {
						// Implement transformers.
						if (right instanceof AddExpr) {
							fallState.putIntervalForVar(varName, Interval.plus(i1, i2));
						}
					}
				}
				
				// ...
			}
			// ...
		} else if (s instanceof JInvokeStmt) {
			// A method is called. e.g. AircraftControl.adjustValue
			
			// You need to check the parameters here.
			InvokeExpr expr = s.getInvokeExpr();
			if (expr.getMethod().getName().equals("adjustValue")) {
				// TODO: Check that is the method from the AircraftControl class.
				
				// TODO: Check that the values are in the allowed range (we do this while computing fixpoint).
				//System.out.println(expr.getArg(0) + " " + expr.getArg(1));
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
	}
	
	Interval tryGetIntervalForValue(IntervalPerVar currentState, Value v) {
		if (v instanceof IntConstant) {
			IntConstant c = ((IntConstant)v);
			return new Interval(c.value, c.value);
		} else if (v instanceof JimpleLocal) {
			JimpleLocal l = ((JimpleLocal)v);
			return currentState.getIntervalForVar(l.getName());
		}
		return null;
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
		trg.copyFrom(src1);
		//System.out.printf("Merge:\n    %s\n    %s\n    ============\n    %s\n",
		//		src1.toString(), src2.toString(), trg.toString());
	}

	@Override
	protected IntervalPerVar newInitialFlow() {
		return new IntervalPerVar();
	}
}
