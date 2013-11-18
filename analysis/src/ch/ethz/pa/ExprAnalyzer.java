package ch.ethz.pa;

import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.IntConstant;

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


}
