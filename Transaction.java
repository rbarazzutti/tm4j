// Runnable or Callable<V>
// TODO interface/abstract better?
public class Transaction implements Runnable {
  public void run() {}
  public native void execute();
  public static native void execute(Runnable r);
  public static native boolean hasRTMSupport();
 
  static {
    System.loadLibrary("tm-tsx");
  }
}
