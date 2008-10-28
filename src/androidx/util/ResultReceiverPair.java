package androidx.util;

public final class ResultReceiverPair<T> implements ResultReceiver<T> {
	private ResultReceiver<T> firstResultReceiver;
	private ResultReceiver<T> secondResultReceiver;
	
	public ResultReceiverPair(ResultReceiver<T> firstResultReceiver, ResultReceiver<T> secondResultReceiver) {
		this.firstResultReceiver = firstResultReceiver;
		this.secondResultReceiver = secondResultReceiver;
	}
	
	public void onFailure(Throwable t) {
		firstResultReceiver.onFailure(t);
		secondResultReceiver.onFailure(t);
	}

	public void onSuccess(T result) {
		firstResultReceiver.onSuccess(result);
		secondResultReceiver.onSuccess(result);
	}
}
