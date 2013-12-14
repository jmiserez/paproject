package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import test.util.CloneOutputStream;
import ch.ethz.pa.Analysis;
import ch.ethz.pa.Verifier;

public class RealTests {

	@Test
	public void TestSoundBranch1_305_at_testBranch1() {
		String result = verify("TestSoundBranch1_305_at_testBranch1");
		assertEquals(result, "Program is UNSAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen1_224_at_testL_OK_0() {
		String result = verify("TestSimpleGen1_224_at_testL_OK_0");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_376_at_testM_OK_4() {
		String result = verify("TestSimpleGen11_376_at_testM_OK_4");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_380_at_testOAMu_OK_8() {
		String result = verify("TestSimpleGen11_380_at_testOAMu_OK_8");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen10_199_at_testRRR_OK_24() {
		String result = verify("TestSimpleGen10_199_at_testRRR_OK_24");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen10_187_at_testRRR_OK_12() {
		String result = verify("TestSimpleGen10_187_at_testRRR_OK_12");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen10_176_at_testROR_AboveEnd_1() {
		String result = verify("TestSimpleGen10_176_at_testROR_AboveEnd_1");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen10_177_at_testROR_Negative_2() {
		String result = verify("TestSimpleGen10_177_at_testROR_Negative_2");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_372_at_testu_OK_0() {
		String result = verify("TestSimpleGen11_372_at_testu_OK_0");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_375_at_testu_NegativeAboveEnd_3() {
		String result = verify("TestSimpleGen11_375_at_testu_NegativeAboveEnd_3");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_377_at_testM_AboveEnd_5() {
		String result = verify("TestSimpleGen11_377_at_testM_AboveEnd_5");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_378_at_testM_Negative_6() {
		String result = verify("TestSimpleGen11_378_at_testM_Negative_6");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	// Loop
	@Test
	public void TestSimpleGen11_379_at_testM_NegativeAboveEnd_7() {
		String result = verify("TestSimpleGen11_379_at_testM_NegativeAboveEnd_7");
		assertEquals(result, "Program is SAFE\n", result);
	}
	
	private String verify(String className){
		PrintStream stdout = System.out;
		ByteArrayOutputStream myOut = new ByteArrayOutputStream();
		CloneOutputStream cloningStream = new CloneOutputStream(System.out, myOut);
		System.setOut(new PrintStream(cloningStream));

    	Verifier.main(new String[]{className});

    	System.setOut(stdout);
    	String captured = myOut.toString();
    	return captured;
	}

}
