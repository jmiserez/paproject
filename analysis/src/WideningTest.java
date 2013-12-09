
public class WideningTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = ac.readSensor(5);
		
		while(pressure < 500){
			pressure++;
		}
		
		ac.adjustValue(1, pressure); //SAFE, but with widening this will say UNSAFE
	}

}
