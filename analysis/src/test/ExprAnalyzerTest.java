package test;

import static org.junit.Assert.*;

import org.junit.Test;

import soot.jimple.IntConstant;
import soot.jimple.internal.JAddExpr;
import ch.ethz.pa.ExprAnalyzer;
import ch.ethz.pa.Interval;
import ch.ethz.pa.StmtAnalyzer;

public class ExprAnalyzerTest {
	
	@Test
	public void testExprAnalyzer() {
		fail("Not yet implemented");
	}

	@Test
	public void testValueToIntervalIntervalValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testValueToIntervalValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testBinExprToState() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseIntConstantIntConstant() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseLocalLocal() {
		fail("Not yet implemented");
	}

	@Test
	public void testDefaultCaseObject() {
		fail("Not yet implemented");
	}

	/*
	 * TODO: This gives me null-ptr exc for some weird reason
	 */
	@Test
	public void testCaseAddExprAddExpr() {
		ExprAnalyzer ea = new ExprAnalyzer(null);
		new JAddExpr(IntConstant.v(1), IntConstant.v(1)).apply(ea);
		assertEquals(new Interval(2), ea.getResult());
	}

	@Test
	public void testCaseMulExprMulExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseSubExprSubExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseEqExprEqExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseNeExprNeExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseGeExprGeExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseGtExprGtExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseLeExprLeExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseLtExprLtExpr() {
		fail("Not yet implemented");
	}

	@Test
	public void testSelect() {
		fail("Not yet implemented");
	}

	@Test
	public void testCaseIntConstantIntConstant1() {
		fail("Not yet implemented");
	}

}
