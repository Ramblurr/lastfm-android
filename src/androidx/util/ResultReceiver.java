package androidx.util;

public interface ResultReceiver<T> {
  void resultObtained(T result);
  public void handle_exception(Throwable t);
}
