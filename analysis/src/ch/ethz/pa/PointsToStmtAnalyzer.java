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
import soot.jimple.NullConstant;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

public class PointsToStmtAnalyzer extends AbstractStmtSwitch {
	
	public static int globalLineNumber = 0;
	
	protected ObjectSetPerVar in;
	protected ObjectSetPerVar out;
	
	public PointsToStmtAnalyzer(ObjectSetPerVar in, ObjectSetPerVar out) {
		this.in = in;
		this.out = out;
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
		LineNumberTag lnt = (LineNumberTag) stmt.getTag("PaLineNumber");
		Value lval = stmt.getLeftOp();
		Value rval = stmt.getRightOp();
		System.out.println("Line " + lnt.getLineNumber() + ": " + lval.getClass().getName() +" ("+lval.getType()+")" + " <- " + rval.getClass().getName());
		HashSet<Tag> rvar = new HashSet<Tag>();
		if (lval instanceof JimpleLocal) {
			JimpleLocal llocal = ((JimpleLocal)lval);
			String varName = llocal.getName();
			Type varType = llocal.getType();
			if(varType instanceof RefType && "AircraftControl".equals(((RefType)varType).getClassName())){
				out.putObjectSetForVar(varName, rvar);
				Set<Tag> lptrs = out.getObjectSetForVar(varName);
				// no need to use ExprAnalyzer here, we only need to handle 3 cases
				if(rval instanceof JNewExpr){
					//remember we are doing flow-INsensitive analysis, so we need to take the union
					lptrs.add(lnt);
					System.err.println("DEBUG: ADD POINTER: "+varName+" -(new)-> "+lnt.getLineNumber());
				} else if (rval instanceof JimpleLocal){
					JimpleLocal rlocal = ((JimpleLocal)rval);
					lptrs.addAll(in.getObjectSetForVar(rlocal.getName()));
					System.err.println("DEBUG: ADD POINTER: "+varName+" -(var)> "+rlocal.getName()+"-(alias)->"+in.getObjectSetForVar(rlocal.getName()));
				} else if (rval instanceof NullConstant){
					// do not add anything
				} else {
					//TODO:unsupported, we do not know what object is assigned to this variable. Assume all of them (TOP)?
					System.err.println("rval not instanceof JNewExpr or JimpleLocal!");
				}
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
