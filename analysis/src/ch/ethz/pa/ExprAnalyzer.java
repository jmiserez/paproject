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
		Value a1 = v.getOp1(),
			  a2 = v.getOp2();
		Interval r1 = Interval.pairEq(valueToInterval(a1), valueToInterval(a2)),
				 r2 = Interval.pairNe(valueToInterval(a1), valueToInterval(a2));
		IntervalPerVar s1 = select(a1, r1, sa.currentState);
		IntervalPerVar s2 = select(a2, r2, sa.currentState);
	}
	
	private static class SelectSwitch extends AbstractJimpleValueSwitch {
		IntervalPerVar result;
		private Interval r;
		private IntervalPerVar m;
		
		SelectSwitch(Interval r, IntervalPerVar m) {
			this.r = r;
			this.m = m;
		}
		@Override
		public void caseIntConstant(IntConstant a) {
			result = new IntervalPerVar(); // BOT
		}
		
		@Override
		public void caseLocal(Local a) {
			result = m.copy();
			String varName = a.getName();
			Interval ma = m.getIntervalForVar(varName);
			Interval b = r.meet(ma);
			m.putIntervalForVar(varName, b);
		}
	}
	
	public static IntervalPerVar select(Value a, Interval r, IntervalPerVar m) {
		SelectSwitch s = new SelectSwitch(r, m);
		a.apply(s);
		return s.result;
	}
}
