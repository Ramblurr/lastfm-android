package fm.last.events;

import fm.last.events.Event.EventResult;

public interface EventHandler
{
	void onSuccess( EventResult result );
	void onError( String error );
}
