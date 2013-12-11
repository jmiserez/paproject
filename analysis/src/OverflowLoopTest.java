
public class OverflowLoopTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int sensorId = 15;
		
		for (int i = 0; i < 30; i++) {
			sensorId += 2147483647;
		}
		
		System.out.println(sensorId);
		
		ac.readSensor(sensorId); //UNSAFE. Even with widening, this should not print SAFE. Wraparound should never happen after widening.
	}

}
