public class TestSoundBranch1_305_at_testBranch1{
  public static void main(String[] args) {
  	testBranch1();
  }
	public static void testBranch1() {
		for (int a = 0; a <= 15; ++a) {
			// MIN_INT = -2147483648			
			// MAX_INT = 2147483647
			int b = a + 2147483632;  // MAX_INT - 15			
			if (b <= 2147483647) {
				int c = b - 2147483632;
				new AircraftControl().readSensor(c + 1);
			}
		}
	}

}