
public class OverflowLoopTest {
	
	public static void main() {
		AircraftControl ac = new AircraftControl();
		int sensorId = 15;
		
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		sensorId += 2147483647; 
		sensorId += 2147483647;
		sensorId += 1;
		
		//adding this makes the program UNSAFE
//		sensorId += 2147483640; 
//		sensorId += 2147483640;
//		sensorId += 15;
		
//		System.out.println(sensorId);
		
		ac.readSensor(sensorId); //SAFE
	}

}
