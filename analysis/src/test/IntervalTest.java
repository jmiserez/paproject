package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.pa.Interval;

public class IntervalTest {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPlus() {
		assertEquals(new Interval(2, 2), new Interval(1, 1).plus(new Interval(1, 1)));
		assertEquals(new Interval(0, 2), new Interval(-1, 1).plus(new Interval(1, 1)));
		assertEquals(new Interval(-2, -2), new Interval(-1, -1).plus(new Interval(-1, -1)));
	}

	@Test
	public void testMinus() {
		assertEquals(new Interval(0, 0), new Interval(1, 1).minus(new Interval(1, 1)));
		assertEquals(new Interval(-2, 0), new Interval(-1, 1).minus(new Interval(1, 1)));
		assertEquals(new Interval(0, 0), new Interval(-1, -1).minus(new Interval(-1, -1)));
		
	}

	@Test
	public void testMultiply() {
		assertEquals(new Interval(1, 1), new Interval(1, 1).multiply(new Interval(1, 1)));
		assertEquals(new Interval(-1, 1), new Interval(-1, 1).multiply(new Interval(1, 1)));
		assertEquals(new Interval(1, 1), new Interval(-1, -1).multiply(new Interval(-1, -1)));
	}

	@Test
	public void testJoin() {
		assertEquals(new Interval(1, 1), new Interval(1, 1).join(new Interval(1, 1)));
		assertEquals(new Interval(-1, 1), new Interval(-1, 1).join(new Interval(1, 1)));
		assertEquals(new Interval(-2, 1), new Interval(-1, -1).join(new Interval(-2, 1)));
		assertEquals(new Interval(-5, 5), new Interval(-5, 5).join(new Interval(-2, 3)));
		assertEquals(Interval.TOP, new Interval(1, 1).join(Interval.TOP));
		assertEquals(new Interval(1, 1), new Interval(1, 1).join(Interval.BOT));
		assertEquals(Interval.BOT, Interval.BOT.join(Interval.BOT));
	}
	
	@Test
	public void testMeet() {
		assertEquals(new Interval(1, 1), new Interval(1, 1).meet(new Interval(1, 1)));
		assertEquals(new Interval(1, 1), new Interval(-1, 1).meet(new Interval(1, 1)));
		assertEquals(new Interval(-1, -1), new Interval(-1, -1).meet(new Interval(-2, 1)));
		assertEquals(new Interval(-2, 3), new Interval(-5, 5).meet(new Interval(-2, 3)));
		assertEquals(new Interval(-6, 6), new Interval(-6, 6).meet(Interval.TOP));
		assertEquals(Interval.TOP, new Interval(-6, 6).join(Interval.TOP));
		assertEquals(new Interval(1, 1), new Interval(1, 1).meet(Interval.TOP));
		assertEquals(Interval.BOT, new Interval(1, 1).meet(new Interval(2, 2)));
		assertEquals(Interval.BOT, new Interval(1, 1).meet(Interval.BOT));
		assertEquals(Interval.BOT, Interval.BOT.meet(Interval.BOT));
	}

}
