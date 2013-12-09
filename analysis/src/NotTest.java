
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class NotTest {
	public static void adjustPressure() {
		AircraftControl ac = new AircraftControl();
		int pressure = ac.readSensor(5);
		
		pressure = ~pressure; //translates to pressure = pressure ^ -1;
		
		ac.adjustValue(5, pressure);
	}
}
