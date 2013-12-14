public class TestSimpleGen10_176_at_testROR_AboveEnd_1{
  public static void main(String[] args) {
    testROR_AboveEnd_1();
  }
  public static void testROR_AboveEnd_1() {
    int start = 45;
    // start = [45,45]  (checked values 45 45 45)
    for (int i0 = -3; i0 <= 3; ++i0) {
      int x1 = start >> (i0 + 3);
      // x1 = [0,45]
      int x2 = x1 * i0;
      // x2 = [-135,135]
      int x3 = x2 << (i0 + 3);
      // x3 = [-8640,8640]
      int x4 = x3 >> (i0 + 3);
      // x3 = [-8640,8640]
      int x5 = x3 >> (i0 + 3);
      // x3 = [-8640,8640]
      int x6 = x3 << (i0 + 4);
      // x6 = [-1105920,1105920]
      // x6 = [-1105920,1105920]
      int x8 = x6 << (i0 + 3);
      // x8 = [-70778880,70778880]
      // x8 = [-70778880,70778880]
      int x10 = x8 * i0;
      // x10 = [-212336640,212336640]
      // x10 = [-212336640,212336640]  (checked values 810 384 352)
      for (int i11 = 0; i11 < 8; ++i11) {
        int x12 = i11 | x10;
        // x12 = [-2147483648,2147483647]  (checked values 810 391 358)
        for (int i13 = -3; i13 <= 3; ++i13) {
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]
          // x12 = [-2147483648,2147483647]  (checked values 810 391 358)
          int index24 = 0;
          if (x12 == 810) { index24 = -998; }
          if (x12 == 391) { index24 = 1000; }
          if (x12 == 358) { index24 = 1000; }
          new AircraftControl().adjustValue(11, index24);
        }
      }
    }
  }

  // Test method with Negative array accesses
}