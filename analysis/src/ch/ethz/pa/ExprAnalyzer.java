package ch.ethz.pa;

import ch.ethz.pa.Interval.PairSwitch;
import soot.Local;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.SubExpr;
import soot.toolkits.scalar.Pair;

public class ExprAnalyzer extends AbstractJimpleValueSwitch {
	StmtAnalyzer sa;
	Interval result;
	
	/*
	 * Expression visitor, result is stored in local variable result
	 */
	public ExprAnalyzer(StmtAnalyzer sa) {
		this.sa = sa;
	}
	
    /*
     * Called for any type of value
     */
    public void valueToInterval(Interval ival, Value val) {
        Interval temp = result;
        result = ival;
        val.apply(this);
        result = temp;
    }
    
    /*
     * Called for any type of value
     */
    public Interval valueToInterval(Value val) {
        Interval temp = result;
        Interval ret = result = new Interval();
        val.apply(this);
        result = temp;
        return ret;
    }
    
    /*
     * Called for any type of value
     */
    public void binExprToState(IntervalPerVar ipv, Value val) {
        Interval temp = result;
        result = new Interval();
        val.apply(this);
        result = temp;
    }
    
	@Override
	public void caseIntConstant(IntConstant v) {
		result.copyFrom(new Interval(v.value));
	}

	@Override
	public void caseLocal(Local v) {
		result.copyFrom(sa.getLocalVariable(v));
	}

	@Override
	public void defaultCase(Object v) {
		result = Interval.BOT;
	}

	@Override
	public void caseAddExpr(AddExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).plus(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).minus(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).multiply(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		doCondExpr(v);
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		doCondExpr(v);
	}
	
	@Override
	public void caseGeExpr(GeExpr v) {
		doCondExpr(v);
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		doCondExpr(v);
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		doCondExpr(v);
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		doCondExpr(v);
	}

	/*
	 * Calculate and update states based on the condition.
	 */
	private void doCondExpr(ConditionExpr c) {
		Value a1 = c.getOp1(),
			  a2 = c.getOp2();
		PairSwitch ps = new Interval.PairSwitch(valueToInterval(a1), valueToInterval(a2));
		c.apply(ps);
		
		Pair<Interval, Interval> p = ps.fallOut;
		Interval r1 = p.getO1(),
				 r2 = p.getO2();
		IntervalPerVar s1 = select(a1, r1, sa.currentState);
		IntervalPerVar s2 = select(a2, r2, sa.currentState);
		IntervalPerVar.meet(s1, s2, sa.fallState);
		
		p = ps.branchOut;
		r1 = p.getO1();
	    r2 = p.getO2();
		s1 = select(a1, r1, sa.currentState);
		s2 = select(a2, r2, sa.currentState);
		IntervalPerVar.meet(s1, s2, sa.branchState);
	}
	
	/*
	 * Visitor for the switch function
	 */
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
	
	/*
	 * Switch function from lecture notes.
	 */
	public static IntervalPerVar select(Value a, Interval r, IntervalPerVar m) {
		SelectSwitch s = new SelectSwitch(r, m);
		a.apply(s);
		return s.result;
	}
}
