public class TestSimpleGen1_224_at_testL_OK_0{
  public static void main(String[] args) {
    testL_OK_0();
  }
  public static void testL_OK_0() {
    int start = 1;
    // start = [1,1]  (checked values 1 1 1)
    for (int x0 = 0; x0 < 1000000000; ++x0) {
      int y1 = (x0 == 32) ? (start * start) : (2 * start);
      // y1 = [1,2]  (checked values 2 1 1)
      int index2 = 0;
      if (y1 == 2) { index2 = 0; }
      if (y1 == 1) { index2 = 15; }
      if (y1 == 1) { index2 = 15; }
      if (y1 == 0) { index2 = -1; }
      if (y1 == 3) { index2 = 16; }
      new AircraftControl().readSensor(index2);
    }
  }

  // Test method with AboveEnd array accesses
}