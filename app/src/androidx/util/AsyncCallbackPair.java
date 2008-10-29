package androidx.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public final class AsyncCallbackPair<T> implements AsyncCallback<T> {
	private AsyncCallback<T> firstResultReceiver;
	private AsyncCallback<T> secondResultReceiver;
	
	public AsyncCallbackPair(AsyncCallback<T> firstResultReceiver, AsyncCallback<T> secondResultReceiver) {
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
