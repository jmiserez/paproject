package ch.ethz.pa;

import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.MulExpr;
import soot.jimple.SubExpr;

public class ExprAnalyzer extends AbstractJimpleValueSwitch {
	StmtAnalyzer sa;
	Interval res_ival;

	public ExprAnalyzer(StmtAnalyzer sa) {
		this.sa = sa;
	}
	
	private class Pair {
		Interval first, second;
		Pair() {
			first = new Interval();
			second = new Interval();
		}
		protected Pair exprToInterval(BinopExpr b) {
			translateExpr(first, b.getOp1());
			translateExpr(second, b.getOp2());
			return this;
		}
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
		Pair p = new Pair().exprToInterval(v);
		res_ival.copyFrom(p.first.plus(p.second));
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		Pair p = new Pair().exprToInterval(v);
		res_ival.copyFrom(p.first.multiply(p.second));
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		Pair p = new Pair().exprToInterval(v);
		res_ival.copyFrom(p.first.minus(p.second));
	}

}
