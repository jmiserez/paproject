package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.pa.domain.Domain;

public class DomainTest {
	@Before
	public void setUp() throws Exception {
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

}
