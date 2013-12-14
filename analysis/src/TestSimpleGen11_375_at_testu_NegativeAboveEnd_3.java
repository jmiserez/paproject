public class TestSimpleGen11_375_at_testu_NegativeAboveEnd_3{
  public static void main(String[] args) {
    testu_NegativeAboveEnd_3();
  }
  public static void testu_NegativeAboveEnd_3() {
    int start = 0;
    // start = [0,0]  (checked values 0 0 0)
    for (int i0 = 15; i0 <= 16; ++i0) {
      if (i0 == 16 && start <= 0) continue;
      int x1 = new AircraftControl().readSensor(i0);
      // x1 = [-999,999]  (checked values 999 -461 -999)
      int index2 = 0;
      if (x1 == 999) { index2 = -1000; }
      if (x1 == -461) { index2 = 1000; }
      if (x1 == -999) { index2 = 1000; }
      if (x1 == -1000) { index2 = -1001; }
      if (x1 == 1000) { index2 = 1001; }
      new AircraftControl().adjustValue(11, index2);
    }
  }

  // Test method with OK array accesses
}