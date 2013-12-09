
public class WideningTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = 0;
		
		while(pressure < 500){
			pressure++;
		}
		
		ac.adjustValue(1, pressure); //SAFE, but with widening this will say UNSAFE
	}

}
