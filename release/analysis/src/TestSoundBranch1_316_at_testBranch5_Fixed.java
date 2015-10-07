public class TestSoundBranch1_316_at_testBranch5_Fixed{
  public static void main(String[] args) {
  	testBranch5_Fixed();
  }
	public static void testBranch5_Fixed() {
		for (int b = 15; b <= 17; ++b) {
			for (int a = 0; a <= 15; ++a) {
				if (a != b) {
					new AircraftControl().readSensor(a);
				} else {
					new AircraftControl().readSensor(0);
				}
			}
		}
	}
	
}