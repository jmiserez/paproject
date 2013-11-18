package ch.ethz.pa;

import soot.Local;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StmtSwitch;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

public class IntervalSwitch implements JimpleValueSwitch, StmtSwitch {

	@Override
	public void caseDoubleConstant(DoubleConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseFloatConstant(FloatConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseIntConstant(IntConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLongConstant(LongConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNullConstant(NullConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseStringConstant(StringConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseClassConstant(ClassConstant v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void defaultCase(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseAddExpr(AddExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseAndExpr(AndExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseDivExpr(DivExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseEqExpr(EqExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNeExpr(NeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseGeExpr(GeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseGtExpr(GtExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLeExpr(LeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLtExpr(LtExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseMulExpr(MulExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseOrExpr(OrExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseRemExpr(RemExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseSubExpr(SubExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseXorExpr(XorExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseCastExpr(CastExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNewExpr(NewExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNegExpr(NegExpr v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseArrayRef(ArrayRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseParameterRef(ParameterRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseThisRef(ThisRef v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseGotoStmt(GotoStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNopStmt(NopStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseRetStmt(RetStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseThrowStmt(ThrowStmt stmt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseLocal(Local l) {
		// TODO Auto-generated method stub

	}

}
