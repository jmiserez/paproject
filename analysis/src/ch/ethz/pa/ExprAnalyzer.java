package ch.ethz.pa;

import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.IntConstant;
import soot.jimple.MulExpr;
import soot.jimple.SubExpr;

public class ExprAnalyzer extends AbstractJimpleValueSwitch {
	StmtAnalyzer sa;
	Interval res_ival;

	public ExprAnalyzer(StmtAnalyzer sa) {
		this.sa = sa;
	}
	
    // Called for any type of value
    public void translateExpr(Interval ival, Value val) {
        Interval temp = res_ival;
        res_ival = ival;
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
		Interval i1 = new Interval();
		Interval i2 = new Interval();
		translateExpr(i1, v.getOp1());
		translateExpr(i2, v.getOp2());
		res_ival.copyFrom(i1.plus(i2));
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		Interval i1 = new Interval();
		Interval i2 = new Interval();
		translateExpr(i1, v.getOp1());
		translateExpr(i2, v.getOp2());
		res_ival.copyFrom(i1.multiply(i2));
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		Interval i1 = new Interval();
		Interval i2 = new Interval();
		translateExpr(i1, v.getOp1());
		translateExpr(i2, v.getOp2());
		res_ival.copyFrom(i1.minus(i2));
	}

}
