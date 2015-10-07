
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ExampleTest4 {
	public static void adjustPressure() {
		AircraftControl ac = new AircraftControl();
		int pressure = ac.readSensor(5);
		
		//pressure = Domain(5,10)
		
		
		for (int i = 0; i < 16 * 1024 * 1024; ++i) {
			pressure = (pressure * 11) ^ i;
		}
		if (pressure < 1000 && pressure > -1000) {
			ac.adjustValue(5, pressure);
		}
	}
}
