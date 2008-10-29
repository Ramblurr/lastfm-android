package androidx.util;

public abstract class DoLater implements GUITask {
	private long millis;
	
	public DoLater(long millisToWaitBeforeDoing) {
		this.millis = millisToWaitBeforeDoing;
	}
	
	public final void after_execute() {
		this.execute();
	}

	public abstract void execute();
	
	public final void executeNonGuiTask() throws Exception {
		if (millis > 0) {
			Thread.sleep(millis);
		}
	}

	public void onFailure(Throwable t) {
	}

}
