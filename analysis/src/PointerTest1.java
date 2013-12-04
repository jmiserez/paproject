
public class PointerTest1 {
	public static void test() {
		AircraftControl ac1 = new AircraftControl();
		AircraftControl ac2 = new AircraftControl();
		
		ac2.adjustValue(1, 1);
		
		ac1 = ac2;
		
		ac1.adjustValue(1, 1);
		
	}
}
