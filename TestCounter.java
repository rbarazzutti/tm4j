public class TestCounter extends Thread
{
  private static final int loops = 1024*1024;
  public static volatile long global_counter = 0;

  public class TxExec extends Transaction
  {
    public void run()
    {
      global_counter++;
    }
  }

  public void run()
  {
    TxExec exec = new TxExec();
    for (int i = 0; i < loops; i++) {
      exec.execute();
    }
  }

  public static boolean isValid()
  {
    if (global_counter != loops * nb_threads) {
      System.err.println("Failed (actual:"+global_counter+" expected:"+(loops * nb_threads)+")");
      return false;
    }
    return true;
  }

  public static int nb_threads = 1;

  public static void main(String[] args)
  {
    if (args.length >= 1) {
      try {
        nb_threads = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.err.println("Invalid number of threads: "+args[0]);
      }
    }
    Thread threads[] = new Thread[nb_threads];

    for (int i = 0; i < nb_threads; i++) {
      threads[i] = new TestCounter();
    }
    for (int i = 0; i < nb_threads; i++) {
      threads[i].start();
    }
    for (int i = 0; i < nb_threads; i++) {
      try {
        threads[i].join();
      } catch (Exception e) {
      }
    }
    if (!isValid())
      System.exit(1);
  }
}
