public class TestLambda
{
  public static long global_counter = 0;
  public static void main(String [] args)
  {
    Transaction.execute(() -> {
      global_counter++;
    });
    if (global_counter != 1) {
      System.err.println("Failed");
      System.exit(1);
    }
  }
}
