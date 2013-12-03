package ch.ethz.pa;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

public class PointsToStmtAnalyzer extends AbstractStmtSwitch {
	
	protected ObjectSetPerVar in;
	protected ObjectSetPerVar out;
	private PointsToExprAnalyzer ea;
	
	public PointsToStmtAnalyzer(ObjectSetPerVar in, ObjectSetPerVar out) {
		this.in = in;
		this.out = out;
		this.ea = new PointsToExprAnalyzer(this);
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
		LineNumberTag lnt = (LineNumberTag) stmt.getTag("LineNumberTag");
		Value lval = stmt.getLeftOp();
		Value rval = stmt.getRightOp();
		System.out.println(lval.getClass().getName() +" ("+lval.getType()+")" + " <- " + rval.getClass().getName());
		HashSet<Tag> rvar = new HashSet<Tag>();
		if (lval instanceof JimpleLocal) {
			JimpleLocal llocal = ((JimpleLocal)lval);
			String varName = llocal.getName();
			Type varType = llocal.getType();
			if(varType instanceof RefType && "AircraftControl".equals(((RefType)varType).getClassName())){
				out.putObjectSetForVar(varName, rvar);
				ea.valueToObjectSet(rvar, rval, lnt);
			}
		}
		// else do nothing
	}

	public Set<Tag> getLocalVariable(Local v) {
		return in.getObjectSetForVar(((Local)v).getName());
	}

	@Override
	public void defaultCase(Object obj) {
		// do nothing
	}
}
