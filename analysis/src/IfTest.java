
public class IfTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int value = 0;
		if (value != 0)
			value = 1001;
		ac.adjustValue(1, value); // SAFE
	}

}
