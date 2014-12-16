import java.util.concurrent.atomic.AtomicLong;

public class Main extends Thread
{
  public volatile long local_counter = 0;
  public static volatile long global_counter = 0;
  public static AtomicLong atomic_counter = new AtomicLong(0);

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
    for (int i = 0; i < 1024*1024; i++) {
      local_counter++;
      //global_counter++;
      exec.execute();
      atomic_counter.getAndIncrement();
    }
    System.out.println("local_counter="+local_counter);
  }

  public static void main(String[] args) {
    int nb_threads = 1;
    if (args.length >= 1) {
      try {
        nb_threads = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.err.println("Invalid number of threads: "+args[0]);
      }
    }
    Thread threads[] = new Thread[nb_threads];

    for (int i = 0; i < nb_threads; i++) {
      threads[i] = new Main();
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

    System.out.println("global_counter="+global_counter);
    System.out.println("atomic_counter="+atomic_counter.get());
  }
}
