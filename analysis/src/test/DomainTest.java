package test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.pa.ExprAnalyzer;
import ch.ethz.pa.domain.Domain;
import ch.ethz.pa.domain.AbstractDomain;
import soot.Type;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;
import soot.toolkits.scalar.Pair;
import soot.util.Switch;

public class DomainTest {
	
	ExprAnalyzer ea;
	static Value dummyVal;
	
	@Before
	public void setUp() throws Exception {
		ea = new ExprAnalyzer(null);
		dummyVal = IntConstant.v(0);
	}

	@After
	public void tearDown() throws Exception {
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
	
	@SuppressWarnings("unchecked")
	private static <T extends AbstractJimpleIntBinopExpr> Pair<AbstractDomain, AbstractDomain> testPairEq(T clazz, AbstractDomain a1, AbstractDomain a2) {
		Domain.PairSwitch ps = new Domain.PairSwitch(a1, a2);
		//T le = (T) clazz.getClass().newInstance();
		//T le = new T(dummyVal, dummyVal);
		//le.apply(ps);
		return ps.branchOut;
	}
	
	@Test
	public void testPairEq() {
		Pair<AbstractDomain, AbstractDomain> expected = new Pair<AbstractDomain, AbstractDomain>(new Domain(3,4), new Domain(3, 4));
		//assertEquals(expected, testPairEq(JEqExpr.class, new Domain(3,7), new Domain(0, 4)));
	}
	
	@Test
	public void testPairNe() {
		Domain.PairSwitch ps = new Domain.PairSwitch(new Domain(3,7), new Domain(0, 4));
		Pair<AbstractDomain, AbstractDomain> expected = new Pair<AbstractDomain, AbstractDomain>(new Domain(3,7), new Domain(0, 4));
		JLeExpr le = new JLeExpr(dummyVal, dummyVal);
		le.apply(ps);
		assertEquals(expected, ps.branchOut);
		
	}
	
	@Test
	public void testPairLe() {
		Domain.PairSwitch ps = new Domain.PairSwitch(new Domain(3,7), new Domain(0, 4));
		Pair<AbstractDomain, AbstractDomain> expected = new Pair<AbstractDomain, AbstractDomain>(new Domain(3,7), new Domain(0, 4));
		JLeExpr le = new JLeExpr(dummyVal, dummyVal);
		le.apply(ps);
		assertEquals(expected, ps.branchOut);
	}
	
	@Test
	public void testPairGe() {
		
	}
	
	@Test
	public void testPairGt() {
		
	}
	
	@Test
	public void testPairLt() {
		
	}

}
