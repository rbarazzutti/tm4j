public class TestRTM
{
  public static void main(String [] args)
  {
    if (!Transaction.hasRTMSupport()) {
      System.err.println("No RTM/TSX support");
      System.exit(1);
    }
    System.out.println("RTM/TSX supported");
  }
}
