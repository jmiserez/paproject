
public class PointerTest2 {
	public static void test() {
		AircraftControl ac = new AircraftControl();
		int value = ac.readSensor(0);
		if (value < 0) {
			ac = new AircraftControl();
		} else {
			ac = new AircraftControl();
			ac.adjustValue(1, value);
		}
		ac.adjustValue(0, value);
	}
}
