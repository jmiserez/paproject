
class AircraftControl {
	public native int readSensor(int sensorId);
	public native void adjustValue(int sensorId, int newValue);

	public AircraftControl() {}
}
