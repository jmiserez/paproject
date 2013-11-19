package ch.ethz.pa;

import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.EqExpr;
import soot.jimple.IntConstant;
import soot.jimple.MulExpr;
import soot.jimple.SubExpr;

public class ExprAnalyzer extends AbstractJimpleValueSwitch {
	StmtAnalyzer sa;
	Interval res_ival;
	Restriction r1, r2;
	

	public ExprAnalyzer(StmtAnalyzer sa) {
		this.sa = sa;
	}
	
	public class Restriction {
		String varName;
		Interval fallRes, branchRes;
	}
	
    // Called for any type of value
    public void valueToInterval(Interval ival, Value val) {
        Interval temp = res_ival;
        res_ival = ival;
        val.apply(this);
        res_ival = temp;
    }
    
    // Called for any type of value
    public Interval valueToInterval(Value val) {
        Interval temp = res_ival;
        Interval ret = res_ival = new Interval();
        val.apply(this);
        res_ival = temp;
        return ret;
    }
    
    // Called for any type of value
    public void binExprToState(IntervalPerVar ipv, Value val) {
        Interval temp = res_ival;
        res_ival = new Interval();
        val.apply(this);
        res_ival = temp;
    }
    
	@Override
	public void caseIntConstant(IntConstant v) {
		res_ival.copyFrom(new Interval(v.value));
	}

	/* (non-Javadoc)
	 * @see soot.jimple.AbstractJimpleValueSwitch#caseLocal(soot.Local)
	 */
	@Override
	public void caseLocal(Local v) {
		res_ival.copyFrom(sa.getLocalVariable(v));
	}

	@Override
	public void defaultCase(Object v) {
		res_ival = Interval.BOT;
	}

	/* (non-Javadoc)
	 * @see soot.jimple.AbstractJimpleValueSwitch#caseAddExpr(soot.jimple.AddExpr)
	 */
	@Override
	public void caseAddExpr(AddExpr v) {
		res_ival.copyFrom(valueToInterval(v.getOp1()).plus(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		res_ival.copyFrom(valueToInterval(v.getOp1()).minus(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		res_ival.copyFrom(valueToInterval(v.getOp1()).multiply(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		
	}
}
