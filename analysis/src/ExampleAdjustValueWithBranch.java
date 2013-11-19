
/**
 * And example application you may want to analyze to test your analysis.
 *
 */
public class ExampleAdjustValueWithBranch {
	public static void adjustSpeed() {
		AircraftControl ac = new AircraftControl();
		
		int i = 2;
		if(i < 2){
			//i : pair_lt(i,[2]) : [bot]
			ac.adjustValue(i, 2);
			i = 3;
		}
		//i : pair_ge(i,[2]) : [2,3]
		ac.adjustValue(i, 2);
		
	}
}



