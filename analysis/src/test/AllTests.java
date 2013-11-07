package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.pa.Verifier;

public class AllTests {
	 
	@BeforeClass
    public static void setupOnce() {
    }

    @Before
    public void setup() {
    	
    }

    @After
    public void tearDown() {
    	
    }
	
    @Test
    public void testAircraftControl() {
    	String result = verify("AircraftControl");
    	assertEquals(result,  "Program is UNSAFE\n");
    }
	@Test
	public void testExampleTest() {
		String result = verify("ExampleTest");
		assertEquals(result,  "Program is SAFE\n");
	}
	
	private String verify(String className){
		PrintStream stdout = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    	System.setOut(new PrintStream(myOut));

    	Verifier.main(new String[]{className});

    	System.setOut(stdout);
    	String captured = myOut.toString();
    	return captured;
	}

}
