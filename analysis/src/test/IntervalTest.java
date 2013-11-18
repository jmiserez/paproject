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
		assertEquals(new Interval(2, 2), Interval.plus(new Interval(1, 1), new Interval(1, 1)));
		assertEquals(new Interval(0, 2), Interval.plus(new Interval(-1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-2, -2), Interval.plus(new Interval(-1, -1), new Interval(-1, -1)));
	}

	@Test
	public void testMinus() {
		assertEquals(new Interval(0, 0), Interval.minus(new Interval(1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-2, 0), Interval.minus(new Interval(-1, 1), new Interval(1, 1)));
		assertEquals(new Interval(0, 0), Interval.minus(new Interval(-1, -1), new Interval(-1, -1)));
		
	}

	@Test
	public void testMultiply() {
		assertEquals(new Interval(1, 1), Interval.multiply(new Interval(1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-1, 1), Interval.multiply(new Interval(-1, 1), new Interval(1, 1)));
		assertEquals(new Interval(1, 1), Interval.multiply(new Interval(-1, -1), new Interval(-1, -1)));
	}

	@Test
	public void testJoin() {
		assertEquals(new Interval(1, 1), Interval.join(new Interval(1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-1, 1), Interval.join(new Interval(-1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-2, 1), Interval.join(new Interval(-1, -1), new Interval(-2, 1)));
		assertEquals(new Interval(-5, 5), Interval.join(new Interval(-5, 5), new Interval(-2, 3)));
		assertEquals(Interval.TOP, Interval.join(new Interval(1, 1), Interval.TOP));
		assertEquals(new Interval(1, 1), Interval.join(new Interval(1, 1), Interval.BOT));
		assertEquals(Interval.BOT, Interval.join(Interval.BOT, Interval.BOT));
	}
	
	@Test
	public void testMeet() {
		assertEquals(new Interval(1, 1), Interval.meet(new Interval(1, 1), new Interval(1, 1)));
		assertEquals(new Interval(1, 1), Interval.meet(new Interval(-1, 1), new Interval(1, 1)));
		assertEquals(new Interval(-1, -1), Interval.meet(new Interval(-1, -1), new Interval(-2, 1)));
		assertEquals(new Interval(-2, 3), Interval.meet(new Interval(-5, 5), new Interval(-2, 3)));
		assertEquals(new Interval(1, 1), Interval.meet(new Interval(1, 1), Interval.TOP));
		assertEquals(Interval.BOT, Interval.meet(new Interval(1, 1), new Interval(2, 2)));
		assertEquals(Interval.BOT, Interval.meet(new Interval(1, 1), Interval.BOT));
		assertEquals(Interval.BOT, Interval.meet(Interval.BOT, Interval.BOT));
	}

}
