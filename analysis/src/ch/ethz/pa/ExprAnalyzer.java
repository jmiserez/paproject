package ch.ethz.pa;

import java.util.Set;

import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SubExpr;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.Tag;
import soot.toolkits.scalar.Pair;
import ch.ethz.pa.domain.AbstractDomain;
import ch.ethz.pa.domain.AbstractDomain.PairSwitch;
import ch.ethz.pa.domain.Domain;

public class ExprAnalyzer extends AbstractJimpleValueSwitch {
	StmtAnalyzer sa;
	AbstractDomain result;
	ObjectSetPerVar aliases;
	
	private final static AbstractDomain[] SENSOR_ID_INTERVALS;
	static {
		SENSOR_ID_INTERVALS = new Domain[16];
        for (int i = 0; i <= 15; i++){
        	SENSOR_ID_INTERVALS[i] = new Domain(i);
        }
    }
	
	/*
	 * Expression visitor, result is stored in local variable result
	 */
	public ExprAnalyzer(StmtAnalyzer sa, ObjectSetPerVar aliases) {
		this.sa = sa;
		this.aliases = aliases;
	}
	
	/*
	 * Getter for the result value of the .apply(ea)
	 */
	public AbstractDomain getResult() {
		return result;
	}
	
    /*
     * Called for any type of value
     */
    public void valueToInterval(AbstractDomain rvar, Value val) {
    	AbstractDomain temp = result;
        result = rvar;
        val.apply(this);
        rvar.copyFrom(result);
        result = temp;
    }
    
    /*
     * Called for any type of value
     */
    public AbstractDomain valueToInterval(Value val) {
    	AbstractDomain temp = result;
    	AbstractDomain ret = result = new Domain();
        val.apply(this);
        result = temp;
        return ret;
    }
    
    /*
     * Called for any type of value
     */
    public void binExprToState(IntervalPerVar ipv, Value val) {
    	AbstractDomain temp = result;
        result = new Domain();
        val.apply(this);
        result = temp;
    }
    
	@Override
	public void caseIntConstant(IntConstant v) {
		result.copyFrom(new Domain(v.value));
	}

	@Override
	public void caseLocal(Local v) {
		result.copyFrom(sa.getLocalVariable(v));
	}

	/*
	 * Go to TOP for unhandled cases
	 */
	@Override
	public void defaultCase(Object v) {
		System.out.flush();
		System.err.println("Warning: ExprAnalyzer.defaultCase called for: "+v);
		System.err.flush();
		result = new Domain().getTop();
	}

	@Override
	public void caseAddExpr(AddExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).plus(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).multiply(valueToInterval(v.getOp2())));
	}
	
//	@Override
//	public void caseDivExpr(DivExpr v) {
//		result.copyFrom(valueToInterval(v.getOp1()).divide(valueToInterval(v.getOp2())));
//	}
	
//	@Override
//	public void caseRemExpr(RemExpr v) {
//		result.copyFrom(valueToInterval(v.getOp1()).rem(valueToInterval(v.getOp2())));
//	}

	@Override
	public void caseSubExpr(SubExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).minus(valueToInterval(v.getOp2())));
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
	
	@Override
	public void caseAndExpr(AndExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).and(valueToInterval(v.getOp2())));
	}
	
	@Override
	public void caseOrExpr(OrExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).or(valueToInterval(v.getOp2())));
	}
	
//	@Override
//	public void caseXorExpr(XorExpr v) {
//		result.copyFrom(valueToInterval(v.getOp1()).xor(valueToInterval(v.getOp2())));
//	}
	
//	@Override
//	public void caseNegExpr(NegExpr v) {
//		result.copyFrom(valueToInterval(v.getOp()).neg());
//	}
	
	@Override
	public void caseShlExpr(ShlExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).shl(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).shr(valueToInterval(v.getOp2())));
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		result.copyFrom(valueToInterval(v.getOp1()).ushr(valueToInterval(v.getOp2())));
	}
	
	/**
	 * This is the case where the the value is actually read into some variable, i.e.:
	 * 
	 * x = y.readSensor();
	 */
	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		Value bval = v.getBase();
		if (bval instanceof JimpleLocal) {
			JimpleLocal blocal = ((JimpleLocal)bval);
			String varName = blocal.getName();
			Type varType = blocal.getType();
			if(varType instanceof RefType && "AircraftControl".equals(((RefType)varType).getClassName())){
				
				Set<Tag> mayObjects = aliases.getObjectSetForVar(varName);
				
				SootMethod meth = v.getMethod();
				if("adjustValue".equals(meth.getName()) && meth.getParameterCount() == 2){
					handleAdjustValue(v, sa.fallState, mayObjects);
				} else if("readSensor".equals(meth.getName()) && meth.getParameterCount() == 1){
					handleReadSensor(v, sa.fallState, mayObjects);
					// Note: this might just a statement (without an assignment). We still return a new Domain, the StmtAnalyzer decides what to with it
					result = new Domain(-999,999);
				}
			}
		}
	}
	

	/*
	 * Calculate and update states based on the condition.
	 */
	private void doCondExpr(ConditionExpr c) {
		Value a1 = c.getOp1(),
			  a2 = c.getOp2();
		PairSwitch ps = new Domain.PairSwitch(valueToInterval(a1), valueToInterval(a2));
		c.apply(ps);
		
		Pair<AbstractDomain, AbstractDomain> p = ps.fallOut;
		AbstractDomain r1 = p.getO1(),
				 r2 = p.getO2();
		IntervalPerVar s1 = select(a1, r1, sa.currentState);
		IntervalPerVar s2 = select(a2, r2, sa.currentState);
		IntervalPerVar.meet(s1, s2, sa.fallState);
		sa.fallState.unreachable = s1.unreachable || s2.unreachable;
		
		p = ps.branchOut;
		r1 = p.getO1();
	    r2 = p.getO2();
		s1 = select(a1, r1, sa.currentState);
		s2 = select(a2, r2, sa.currentState);
		IntervalPerVar.meet(s1, s2, sa.branchState);
		sa.branchState.unreachable = s1.unreachable || s2.unreachable;
	}
	
	/*
	 * Visitor for the switch function
	 */
	private static class SelectSwitch extends AbstractJimpleValueSwitch {
		IntervalPerVar result;
		private AbstractDomain r;
		private IntervalPerVar m;
		
		SelectSwitch(AbstractDomain r, IntervalPerVar m) {
			this.r = r;
			this.m = m;
		}
		@Override
		public void caseIntConstant(IntConstant a) {
			if (!new Domain(a.value).meet(r).isBot())
				result = m;
			else
				result = new IntervalPerVar(true); // BOT
		}
		
		@Override
		public void caseLocal(Local a) {
			result = m.copy();
			String varName = a.getName();
			AbstractDomain ma = m.getIntervalForVar(varName);
			AbstractDomain b = r.meet(ma);
			result.putIntervalForVar(varName, b);
		}
	}
	
	/*
	 * Switch function from lecture notes.
	 */
	public static IntervalPerVar select(Value a, AbstractDomain r, IntervalPerVar m) {
		SelectSwitch s = new SelectSwitch(r, m);
		a.apply(s);
		return s.result;
	}
	
	protected void handleAdjustValue(InvokeExpr adjustValue, IntervalPerVar m, Set<Tag> mayObjects) {
		Value sensorIdArg = adjustValue.getArg(0);
		Value newValueArg = adjustValue.getArg(1);
		AbstractDomain sensorIdInterval = new Domain();
		AbstractDomain newValueInterval = new Domain();
		this.valueToInterval(sensorIdInterval, sensorIdArg);
		this.valueToInterval(newValueInterval, newValueArg);
		if (!(new Domain(0, 15).contains(sensorIdInterval))){
			throw new ProgramIsUnsafeException("adjustValue sensorId argument was out of range ("+sensorIdInterval.toString()+")");
		}
		if (!(new Domain(-999, 999).contains(newValueInterval))){
			throw new ProgramIsUnsafeException("adjustValue newValue argument was out of range ("+newValueInterval.toString()+")");
		}
		for (int i = 0; i <= 15; i++){
			if(SENSOR_ID_INTERVALS[i].contains(sensorIdInterval)){
				for(Tag t : mayObjects){
					int count = m.getAdjustValueCountForVar(t.toString(), i);
					count++;
					m.putAdjustValueCountForVar(t.toString(), i, count);
					if(count > 1){
						throw new ProgramIsUnsafeException("adjustValue for object "+ t.toString() +" was called more than once");
					}
				}
			}
		}
	}
	
	protected void handleReadSensor(InvokeExpr readSensor, IntervalPerVar m, Set<Tag> mayObjects) {
		Value sensorIdArg = ((InvokeExpr) readSensor).getArg(0);
		AbstractDomain sensorIdInterval = new Domain();
		this.valueToInterval(sensorIdInterval, sensorIdArg);
		if (!(new Domain(0, 15).contains(sensorIdInterval))){
			throw new ProgramIsUnsafeException("readSensor sensorId argument was out of range ("+sensorIdInterval.toString()+")");
		}
		for (int i = 0; i <= 15; i++){
			if(SENSOR_ID_INTERVALS[i].contains(sensorIdInterval)){
				for(Tag t : mayObjects){
					int count = m.getReadSensorCountForVar(t.toString(), i);
					count++;
					m.putReadSensorCountForVar(t.toString(), i, count);
					if(count > 1){
						throw new ProgramIsUnsafeException("readSensor for object "+ t.toString() +" was called more than once");
					}
				}
			}
		}
	}
}
