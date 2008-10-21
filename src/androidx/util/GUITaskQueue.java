package androidx.util;

import fm.last.Log;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

public class GUITaskQueue {
  private static final int HANDLE_EXCEPTION = 0x1337;
  private static final int HANDLE_AFTER_EXECUTE = 0x1338;
  private TaskQueue taskQ;
  private Handler handler;
  private static GUITaskQueue singleton;
  
  public static GUITaskQueue getInstance() {
    if (singleton == null) {
      singleton = new GUITaskQueue();
      singleton.start();
    }
    return singleton;
  }
  
  private GUITaskQueue() {
    taskQ = new TaskQueue();
    handler = new MyHandler();
  }

  public void start() {
    taskQ.start();
  }
  
  public void stop() {
    taskQ.stop();
  }
  
  public void addTask(GUITask task) {
    taskQ.addTask(new GUITaskAdapter(task));
  }
    
  private static class GUITaskWithSomething<T> {
    GUITask guiTask;
    T something;
    
    GUITaskWithSomething(GUITask _guiTask, T _something) {
      guiTask = _guiTask;
      something = _something;
    }
  }
  
  private void postMessage(int what, Object thingToPost) {
    Message msg = new Message();
    msg.obj = thingToPost;
    msg.what = what;
    handler.sendMessage(msg);
  }

  private void postException(GUITask task, Throwable t) {
    postMessage(HANDLE_EXCEPTION, new GUITaskWithSomething(task, t));
  }
  
  private class MyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
        case HANDLE_EXCEPTION:
          GUITaskWithSomething<Throwable> thingie = (GUITaskWithSomething<Throwable>) msg.obj;
          thingie.guiTask.handle_exception(thingie.something);
          break;
          
        case HANDLE_AFTER_EXECUTE:
          GUITask task = (GUITask) msg.obj;
          try {
            task.after_execute();
          } catch (Throwable t) {
            Log.e(t);
          }
          break;
      }
      super.handleMessage(msg);
    }
  }
  
  private class GUITaskAdapter implements Runnable {
    private GUITask task;
    GUITaskAdapter(GUITask _task) {
      task = _task;
    }
    
    public void run() {
      try {
        task.executeNonGuiTask();
        postMessage(HANDLE_AFTER_EXECUTE, task);
      } catch (Throwable t) {
        postException(task, t);
      }
    }
  }
}
