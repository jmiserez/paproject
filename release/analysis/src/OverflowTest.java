
public class OverflowTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = ac.readSensor(5);
		
		while(pressure > 0){
			pressure++;
		}
		
		//the only way to reach this is with overflow
		ac.adjustValue(1, pressure); //UNSAFE
	}

}
