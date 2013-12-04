package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import soot.toolkits.scalar.Pair;
import ch.ethz.pa.ExprAnalyzer;
import ch.ethz.pa.ObjectSetPerVar;
import ch.ethz.pa.domain.AbstractDomain;
import ch.ethz.pa.domain.Domain;

public class DomainTest {
	
	ExprAnalyzer ea;
	static Value dummyVal;
	
	@Before
	public void setUp() throws Exception {
		ea = new ExprAnalyzer(null,new ObjectSetPerVar());
		dummyVal = IntConstant.v(0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testContains() {
		assertTrue(new Domain(0,0).contains(new Domain(0,0)));
		assertTrue(new Domain(-2,2).contains(new Domain(0,0)));
		assertTrue(new Domain(-4,4).contains(new Domain(-2,2)));
		assertTrue(new Domain(1,4).contains(new Domain(2,4)));
		assertTrue(new Domain(0,10).contains(new Domain(1,1)));
		assertTrue(new Domain(0,15).contains(new Domain(1,1)));
		
	}
	
	@Test
	public void testPlus() {
		assertEquals(new Domain(2, 2), new Domain(1, 1).plus(new Domain(1, 1)));
		assertEquals(new Domain(0, 2), new Domain(-1, 1).plus(new Domain(1, 1)));
		assertEquals(new Domain(-2, -2), new Domain(-1, -1).plus(new Domain(-1, -1)));
	}

	@Test
	public void testMinus() {
		assertEquals(new Domain(0, 0), new Domain(1, 1).minus(new Domain(1, 1)));
		assertEquals(new Domain(-2, 0), new Domain(-1, 1).minus(new Domain(1, 1)));
		assertEquals(new Domain(0, 0), new Domain(-1, -1).minus(new Domain(-1, -1)));
		
	}

	@Test
	public void testMultiply() {
		assertEquals(new Domain(1, 1), new Domain(1, 1).multiply(new Domain(1, 1)));
		assertEquals(new Domain(-1, 1), new Domain(-1, 1).multiply(new Domain(1, 1)));
		assertEquals(new Domain(1, 1), new Domain(-1, -1).multiply(new Domain(-1, -1)));
	}

	@Test
	public void testJoin() {
		assertEquals(new Domain(1, 1), new Domain(1, 1).join(new Domain(1, 1)));
		assertEquals(new Domain(-1, 1), new Domain(-1, 1).join(new Domain(1, 1)));
		assertEquals(new Domain(-2, 1), new Domain(-1, -1).join(new Domain(-2, 1)));
		assertEquals(new Domain(-5, 5), new Domain(-5, 5).join(new Domain(-2, 3)));
		assertEquals(new Domain().getTop(), new Domain(1, 1).join(new Domain().getTop()));
		assertEquals(new Domain(1, 1), new Domain(1, 1).join(new Domain().getBot()));
		assertEquals(new Domain().getBot(), new Domain().getBot().join(new Domain().getBot()));
	}
	
	@Test
	public void testMeet() {
		assertEquals(new Domain(1, 1), new Domain(1, 1).meet(new Domain(1, 1)));
		assertEquals(new Domain(1, 1), new Domain(-1, 1).meet(new Domain(1, 1)));
		assertEquals(new Domain(-1, -1), new Domain(-1, -1).meet(new Domain(-2, 1)));
		assertEquals(new Domain(-2, 3), new Domain(-5, 5).meet(new Domain(-2, 3)));
		assertEquals(new Domain(-6, 6), new Domain(-6, 6).meet(new Domain().getTop()));
		assertEquals(new Domain().getTop(), new Domain(-6, 6).join(new Domain().getTop()));
		assertEquals(new Domain(1, 1), new Domain(1, 1).meet(new Domain().getTop()));
		assertEquals(new Domain().getBot(), new Domain(1, 1).meet(new Domain(2, 2)));
		assertEquals(new Domain().getBot(), new Domain(1, 1).meet(new Domain().getBot()));
		assertEquals(new Domain().getBot(), new Domain().getBot().meet(new Domain().getBot()));
	}
	
	private static Pair<AbstractDomain, AbstractDomain> testPairEq(ConditionExpr expr, AbstractDomain a1, AbstractDomain a2) {
		Domain.PairSwitch ps = new Domain.PairSwitch(a1, a2);
		expr.apply(ps);
		return ps.branchOut;
	}
	
	private static Pair<AbstractDomain, AbstractDomain> getExpected(AbstractDomain a1, AbstractDomain a2) {
		return new Pair<AbstractDomain, AbstractDomain>(a1, a2);
	}
	
	@Test
	public void testPair() {
		JEqExpr expr = new JEqExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(3,4), new Domain(3, 4)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3,7)));
	}
	
	@Test
	public void testPairNe() {
		JNeExpr expr = new JNeExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(0, 7), new Domain(0, 7)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3, 7)));
	}
	
	@Test
	public void testPairLe() {
		JLeExpr expr = new JLeExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(0, 4), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3, 7)));
		assertEquals(getExpected(new Domain(0, 4), new Domain(0, 4)), 
				testPairEq(expr, new Domain(0, 4), new Domain(0, 4)));
		assertEquals(getExpected(new Domain(0, 7), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 10), new Domain(3, 7)));
	}
	
	@Test
	public void testPairGe() {
		JGeExpr expr = new JGeExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(3, 4), new Domain(3, 4)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3, 7)));
		assertEquals(getExpected(new Domain(0, 4), new Domain(0, 4)), 
				testPairEq(expr, new Domain(0, 4), new Domain(0, 4)));
		assertEquals(getExpected(new Domain(3, 10), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 10), new Domain(3, 7)));
	}
	
	@Test
	public void testPairGt() {
		JGtExpr expr = new JGtExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(4, 4), new Domain(3, 3)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3, 7)));
		assertEquals(getExpected(new Domain(1, 4), new Domain(0, 3)), 
				testPairEq(expr, new Domain(0, 4), new Domain(0, 4)));
		assertEquals(getExpected(new Domain(4, 10), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 10), new Domain(3, 7)));
	}
	
	@Test
	public void testPairLt() {
		JLtExpr expr = new JLtExpr(dummyVal, dummyVal);
		assertEquals(getExpected(new Domain(0, 3), new Domain(1, 4)), 
				testPairEq(expr, new Domain(0, 4), new Domain(0, 4)));
		assertEquals(getExpected(new Domain(0, 4), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 4), new Domain(3, 7)));
		assertEquals(getExpected(new Domain(0, 6), new Domain(3, 7)), 
				testPairEq(expr, new Domain(0, 10), new Domain(3, 7)));
	}

}
