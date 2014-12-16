// Runnable or Callable<V>
// TODO interface/abstract better?
public class Transaction implements Runnable {
  public void run() {}
  public native void execute();
 
  static {
    System.loadLibrary("tm-tsx");
  }
}
