package androidx.util;

public interface GUITask extends ExceptionHandler {
  public void executeNonGuiTask() throws Exception;
  public void after_execute();
}
