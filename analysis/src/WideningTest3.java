
public class WideningTest3 {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = 0;
		int parameter = 950;
		
		while(pressure < 200){
			pressure++; //--> will go to Integer.MAX_VALUE with widening, and 1150 without it
		}
		
		ac.adjustValue(1, pressure + Integer.MAX_VALUE + 5); //UNSAFE. Even with widening, this should not print SAFE. Wraparound should never happen after widening.
	}

}
