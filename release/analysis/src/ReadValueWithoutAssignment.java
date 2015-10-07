
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ReadValueWithoutAssignment {
	public static void readValueWithoutAssignment() {
		AircraftControl ac = new AircraftControl();
		ac.readSensor(0);
		ac.readSensor(1);
		ac.readSensor(2);
		ac.readSensor(3);
	}
}
