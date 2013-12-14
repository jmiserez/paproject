public class TestSimpleGen11_380_at_testOAMu_OK_8{
  public static void main(String[] args) {
    testOAMu_OK_8();
  }
  public static void testOAMu_OK_8() {
    int start = 0;
    // start = [0,0]  (checked values 0 0 0)
    for (int i0 = 0; i0 < 8; ++i0) {
      int x1 = i0 | start;
      // x1 = [0,7]  (checked values 0 7 6)
      for (int i2 = 0; i2 < 8; ++i2) {
        int x3 = i2 & x1;
        // x3 = [0,7]  (checked values 0 7 4)
        for (int i4 = 99; i4 <= 101; ++i4) {
          int x5 = x3 % i4;
          // x5 = [0,7]  (checked values 0 7 4)
          for (int i6 = 15; i6 <= 16; ++i6) {
            if (i6 == 16 && x5 <= 7) continue;
            int x7 = new AircraftControl().readSensor(i6);
            // x7 = [-999,999]  (checked values 999 706 -999)
            new AircraftControl().adjustValue(11, x7);
          }
        }
      }
    }
  }

  // Test method with AboveEnd array accesses
}