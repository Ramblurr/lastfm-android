package androidx.util;

public interface GUITask {
  public void executeNonGuiTask() throws Exception;
  public void handle_exception(Throwable t);
  public void after_execute();
}
