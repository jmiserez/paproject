
public class WideningTest3 {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = 950;
		int parameter = 0;
		
		while(parameter < 200){
			parameter++;
			pressure++; //--> will go to Integer.MAX_VALUE with widening, and 1150 without it
		}
		
		ac.adjustValue(1, pressure + Integer.MAX_VALUE + 5); //UNSAFE. Even with widening, this should not print SAFE. Wraparound should never happen after widening.
	}

}
