public class TestSimpleGen10_177_at_testROR_Negative_2{
  public static void main(String[] args) {
    testROR_Negative_2();
  }
  public static void testROR_Negative_2() {
    int start = 45;
    // start = [45,45]  (checked values 45 45 45)
    for (int i0 = -3; i0 <= 3; ++i0) {
      int x1 = start << (i0 + 3);
      // x1 = [45,2880]
      int x2 = x1 >> (i0 + 3);
      // x2 = [0,2880]
      int x3 = x2 << (i0 + 4);
      // x3 = [0,368640]
      int x4 = x3 >> (i0 + 3);
      // x3 = [0,368640]
      int x5 = x3 >> (i0 + 3);
      // x3 = [0,368640]
      int x6 = x3 * i0;
      // x6 = [-1105920,1105920]
      int x7 = x6 >> (i0 + 3);
      // x6 = [-1105920,1105920]
      // x6 = [-1105920,1105920]
      int x9 = x6 * i0;
      // x9 = [-3317760,3317760]
      int x10 = x9 * i0;
      // x10 = [-9953280,9953280]
      // x10 = [-9953280,9953280]  (checked values -2430 36 -180)
      for (int i11 = 0; i11 < 8; ++i11) {
        int x12 = i11 | x10;
        // x12 = [-2147483648,2147483647]  (checked values -2430 39 -178)
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
          // x12 = [-2147483648,2147483647]  (checked values -2430 39 -178)
          int index24 = 0;
          if (x12 == -2430) { index24 = -1000; }
          if (x12 == 39) { index24 = 998; }
          if (x12 == -178) { index24 = 998; }
          new AircraftControl().adjustValue(11, index24);
        }
      }
    }
  }

  // Test method with NegativeAboveEnd array accesses
}