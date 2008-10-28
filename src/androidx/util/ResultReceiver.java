package androidx.util;

public interface ResultReceiver<T> {
  void onSuccess(T result);
  public void handle_exception(Throwable t);
}
