public class TestSimpleGen11_372_at_testu_OK_0{
  public static void main(String[] args) {
    testu_OK_0();
  }
  public static void testu_OK_0() {
    int start = 0;
    // start = [0,0]  (checked values 0 0 0)
    for (int i0 = 15; i0 <= 16; ++i0) {
      if (i0 == 16 && start <= 0) continue;
      int x1 = new AircraftControl().readSensor(i0);
      // x1 = [-999,999]  (checked values 999 -204 -999)
      new AircraftControl().adjustValue(11, x1);
    }
  }

  // Test method with AboveEnd array accesses
}