package ch.ethz.pa;

import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.internal.JimpleLocal;

public class StmtVisitor extends AbstractStmtSwitch {
	
	private IntervalPerVar fallState;
	private IntervalPerVar branchState;
	private IntervalPerVar currentState;

	public StmtVisitor(IntervalPerVar currentState, IntervalPerVar fallState, IntervalPerVar branchState) {
		this.fallState = fallState;
		this.branchState = branchState;
		this.currentState = currentState;
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
        Value condition = stmt.getCondition();
        condition.apply(this);
	}
	
	Interval tryGetIntervalForValue(Value v) {
		if (v instanceof IntConstant) {
			IntConstant c = ((IntConstant)v);
			return new Interval(c.value);
		} else if (v instanceof JimpleLocal) {
			JimpleLocal l = ((JimpleLocal)v);
			return currentState.getIntervalForVar(l.getName());
		}
		return Interval.TOP;
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		Value lval = stmt.getLeftOp();
		Value rval = stmt.getRightOp();
		System.out.println(lval.getClass().getName() + " " + rval.getClass().getName());
		
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
				
				AbstractDomain i1 = tryGetIntervalForValue(current, r1);
				AbstractDomain i2 = tryGetIntervalForValue(current, r2);
				
				if (i1 != null && i2 != null) {
					// Implement transformers.
					if (right instanceof AddExpr) {
						fallState.putIntervalForVar(varName, i1.plus(i2));
					}
					if (right instanceof SubExpr) {
						fallState.putIntervalForVar(varName, i1.minus(i2));
					}
					if (right instanceof MulExpr) {
						fallState.putIntervalForVar(varName, i1.multiply(i2));
					}
				}
			} else {
				fallState.putIntervalForVar(varName, new Interval().getTop());
			}
			
			// ...
		}
	}
	
	void handleAssign(DefinitionStmt stmt) {
        Value lval = stmt.getLeftOp();
        Value rval = stmt.getRightOp();
        Variable rvar;
        if (lval instanceof Local) {
            rvar = getLocalVariable((Local)lval);
        } else {
            rvar = jt.makeVariable(rval);
        }
        et.translateExpr(rvar, stmt.getRightOpBox());
        if (lval instanceof ArrayRef) {
            notSupported("We do not support arrays");
        } else if (lval instanceof FieldRef) {
            notSupported("We do not support field references");
        }
    }

	

}
