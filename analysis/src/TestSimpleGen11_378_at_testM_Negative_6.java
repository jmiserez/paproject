public class TestSimpleGen11_378_at_testM_Negative_6{
	public static void main(String[] args) {
		testM_Negative_6();
	}
	public static void testM_Negative_6() {
		int start = 0;
		// start = [0,0]  (checked values 0 0 0)
		int i0 = 99;
		int x1 = start % i0;
		// x1 = [0,0]  (checked values 0 0 0)
		int index2 = 0;
		int tmp3 = 0;
		if (x1 == 0) { tmp3 = new AircraftControl().readSensor(4); }
		if (tmp3 == -999) { index2 = -1000; }
		if (tmp3 == 999) { index2 = 998; }
		if (tmp3 == 0) { index2 = 998; }
		if (x1 == -1) { index2 = -1001; }
		if (x1 == 1) { index2 = 999; }
		new AircraftControl().adjustValue(11, index2);
	}

	// Test method with NegativeAboveEnd array accesses
}