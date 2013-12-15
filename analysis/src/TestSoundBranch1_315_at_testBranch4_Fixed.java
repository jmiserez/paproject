public class TestSoundBranch1_315_at_testBranch4_Fixed{
  public static void main(String[] args) {
  	testBranch4_Fixed();
  }
	public static void testBranch4_Fixed() {
		for (int a = 0; a <= 15; ++a) {
			// MIN_INT = -2147483648			
			// MAX_INT = 2147483647
			int b = a + -2147483648;
			if (b > -2147483648) {
				int c = b - (-2147483648);
				new AircraftControl().readSensor(c - 1);
			}
		}
	}
	
}