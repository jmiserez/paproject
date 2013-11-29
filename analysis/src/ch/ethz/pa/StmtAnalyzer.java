package ch.ethz.pa;

import ch.ethz.pa.domain.AbstractDomain;
import ch.ethz.pa.domain.Domain;
import soot.Local;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JimpleLocal;

public class StmtAnalyzer extends AbstractStmtSwitch {

	protected IntervalPerVar fallState;
	protected IntervalPerVar branchState;
	protected IntervalPerVar currentState;
	private ExprAnalyzer ea;

	public StmtAnalyzer(IntervalPerVar currentState, IntervalPerVar fallState, IntervalPerVar branchState) {
		this.fallState = fallState;
		this.branchState = branchState;
		this.currentState = currentState;
		this.ea = new ExprAnalyzer(this);
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		stmt.getCondition().apply(ea);
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		handleAssign(stmt);
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		handleAssign(stmt);
	}

	private void handleAssign(DefinitionStmt stmt) {
		Value lval = stmt.getLeftOp();
		Value rval = stmt.getRightOp();
		System.out.println(lval.getClass().getName() + " " + rval.getClass().getName());
		AbstractDomain rvar = new Domain();
		if (lval instanceof JimpleLocal) {
			String varName = ((JimpleLocal)lval).getName();
			fallState.putIntervalForVar(varName, rvar);
		}
		ea.valueToInterval(rvar, rval);
	}

	public AbstractDomain getLocalVariable(Local v) {
		return currentState.getIntervalForVar(((Local)v).getName());
	}


	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		// A method is called. e.g. AircraftControl.adjustValue

		// You need to check the parameters here.
		InvokeExpr expr = stmt.getInvokeExpr();
		if (expr.getMethod().getName().equals("adjustValue")) {
			// TODO: Check that this is really the method from the AircraftControl class. (how? -> has two arguments, pointer analysis)
			// TODO: Increment invocation count for THIS AircraftControl object in the global table (pointer analysis)
			ea.handleAdjustValue(expr, fallState); //TODO: is fallState correct??
		} else if(expr.getMethod().getName().equals("readSensor")){
			// TODO: Check that this is really the method from the AircraftControl class. (how? -> has two arguments, pointer analysis)
			// TODO: Increment invocation count for THIS AircraftControl object in the global table (pointer analysis)
			ea.handleReadSensor(expr, fallState); //TODO: is fallState correct??
		}
	}

	@Override
	public void defaultCase(Object obj) {
		//TODO: Set everything to TOP
	}

}
