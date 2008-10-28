package androidx.util;

public final class ResultReceiverPair<T> implements ResultReceiver<T> {
	private ResultReceiver<T> firstResultReceiver;
	private ResultReceiver<T> secondResultReceiver;
	
	public ResultReceiverPair(ResultReceiver<T> firstResultReceiver, ResultReceiver<T> secondResultReceiver) {
		this.firstResultReceiver = firstResultReceiver;
		this.secondResultReceiver = secondResultReceiver;
	}
	
	public void handle_exception(Throwable t) {
		firstResultReceiver.handle_exception(t);
		secondResultReceiver.handle_exception(t);
	}

	public void onSuccess(T result) {
		firstResultReceiver.onSuccess(result);
		secondResultReceiver.onSuccess(result);
	}
}
