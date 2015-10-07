
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ExampleTest3 {
	public static void adjustHeightWithBug() {
		AircraftControl ac = new AircraftControl();
		int height = ac.readSensor(1);
		int power = ac.readSensor(2);
		if (power > 0) {
			if (height < 0) {
				height += power + 1;  // Power can be up to 999, height is <=-1, OK.
			} else {
				height -= power + 1;  // Power can be up to 999, height is >= 0, BUG.
			}
		}
		ac.adjustValue(1, height);
	}
	
}
