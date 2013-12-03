package ch.ethz.pa;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.NewExpr;
import soot.tagkit.Tag;

public class PointsToExprAnalyzer extends AbstractJimpleValueSwitch {
	PointsToStmtAnalyzer sa;
	Set<Tag> result;
	Tag currentLineNumber;

	public PointsToExprAnalyzer(PointsToStmtAnalyzer sa) {
		this.sa = sa;
		this.currentLineNumber = null;
		this.result = new HashSet<Tag>();
	}

	public Set<Tag> getResult() {
		return result;
	}
	
	@Override
	public void caseLocal(Local v) {
		result.clear();
		result.addAll(sa.getLocalVariable(v));
	}
	
	@Override
	public void caseNewExpr(NewExpr v) {
		result.add(currentLineNumber);
	}
	
    /*
     * Called for any type of value
     */
    public void valueToObjectSet(Set<Tag> rvar, Value val, Tag lineNumber) {
    	Set<Tag> temp = result;
    	Tag temp2 = currentLineNumber;
    	currentLineNumber = lineNumber;
        result = rvar;
        val.apply(this);
        rvar.clear(); //TODO: something is seriously wrong here
        rvar.addAll(result);
        currentLineNumber = temp2;
        result = temp;
    }
    
    /*
     * Called for any type of value
     */
    public Set<Tag> valueToObjectSet(Value val, Tag lineNumber) {
    	Set<Tag> temp = result;
    	Tag temp2 = currentLineNumber;
    	Set<Tag> ret = result = new HashSet<Tag>();
        val.apply(this);
        result = temp;
        currentLineNumber = temp2;
        return ret;
    }
    
    /*
     * Called for any type of value
     */
    public void binExprToState(ObjectSetPerVar ipv, Value val) {
    	Set<Tag> temp = result;
        result = new HashSet<Tag>();
        val.apply(this);
        result = temp;
    }
}
