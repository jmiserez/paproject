
public class WideningTest2 {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int pressure = 0;
		int parameter = 1;
		
		while(parameter < 20){
			pressure += parameter++;
		}
		
		ac.adjustValue(1, pressure); //SAFE, but with widening this will say UNSAFE
	}

}
