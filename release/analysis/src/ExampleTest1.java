
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ExampleTest1 {
	public static void adjustSpeed() {
		AircraftControl ac = new AircraftControl();
		int value = ac.readSensor(0);
		if (value < 0) {
			value += 10;
		} else {
			value -= 10;
		}
		ac.adjustValue(0, value);
	}
}
