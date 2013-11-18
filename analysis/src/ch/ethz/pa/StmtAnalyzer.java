package ch.ethz.pa;

import soot.Local;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JimpleLocal;

public class StmtAnalyzer extends AbstractStmtSwitch {
	
	private IntervalPerVar fallState;
	private IntervalPerVar branchState;
	private IntervalPerVar currentState;
	private ExprAnalyzer ea;

	public StmtAnalyzer(IntervalPerVar currentState, IntervalPerVar fallState, IntervalPerVar branchState) {
		this.fallState = fallState;
		this.branchState = branchState;
		this.currentState = currentState;
		this.ea = new ExprAnalyzer(this);
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		// TODO: Write this
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		handleAssign(stmt);
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		handleAssign(stmt);
	}
	
	void handleAssign(DefinitionStmt stmt) {
		Value lval = stmt.getLeftOp();
		Value rval = stmt.getRightOp();
		System.out.println(lval.getClass().getName() + " " + rval.getClass().getName());
        Interval rvar = new Interval();
        if (lval instanceof JimpleLocal) {
        	String varName = ((JimpleLocal)lval).getName();
        	fallState.putIntervalForVar(varName, rvar);
        }
        ea.translateExpr(rvar, rval);
    }

	public Interval getLocalVariable(Local v) {
		return currentState.getIntervalForVar(((Local)v).getName());
	}


	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		// A method is called. e.g. AircraftControl.adjustValue
        
        // You need to check the parameters here.
        InvokeExpr expr = stmt.getInvokeExpr();
        if (expr.getMethod().getName().equals("adjustValue")) {
                // TODO: Check that this is really the method from the AircraftControl class.
                
                // TODO: Check that the values are in the allowed range (we do this while computing fixpoint).
                //System.out.println(expr.getArg(0) + " " + expr.getArg(1));
        } else if(expr.getMethod().getName().equals("readSensor")){
                // TODO: Check that this is really the method from the AircraftControl class.
                checkReadSensorArgument(expr);
        }
	}
	
    protected void checkReadSensorArgument(InvokeExpr readSensor) {
        Value v = ((InvokeExpr) readSensor).getArg(0);
        Interval i = new Interval();
        ea.translateExpr(i, v);
        if (!(new Interval(0, 15).contains(i))){
            //throw new ProgramIsUnsafeException("readSensor argument was out of range ("+i.toString()+")");
        }
}
}
