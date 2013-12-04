
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class MyTest {
	public static void test() {
		test1(5);
	}
	private static void test1(int i) {
		int x = 5;
		int y = 3;
		int z = x;
		//AircraftControl ac = new AircraftControl();
		//int value = ac.readSensor(0); //value: [0, 15]
		if (i >= 0) {
			y++;
			i = i - 1;
		}
		if (i == 10)
			System.out.println("Launch rocket");
		else
			System.out.println("Do not launch rocket");
		System.out.println("Finished");
		i = 2;
		
	}
}
