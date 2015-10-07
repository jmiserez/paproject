
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ExampleTest {
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
	
	public static void adjustHeight() {
		AircraftControl ac = new AircraftControl();
		int height = ac.readSensor(1);
		int power = ac.readSensor(2);
		if (power > 0) {
			if (height < 0) {
				height += power + 1;  // Power can be up to 999, height is <=-1, OK.
			} else {
				height -= power;  // Power can be up to 999, height is >= 0, OK.
			}
		}
		ac.adjustValue(1, height);
	}
	
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
	
	public static void adjustPressure() {
		AircraftControl ac = new AircraftControl();
		int pressure = ac.readSensor(5);
		for (int i = 0; i < 16 * 1024 * 1024; ++i) {
			pressure = (pressure * 11) ^ i;
		}
		if (pressure < 1000 && pressure > -1000) {
			ac.adjustValue(5, pressure);
		}
	}
}
