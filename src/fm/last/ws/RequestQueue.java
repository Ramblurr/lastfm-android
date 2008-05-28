package fm.last.ws;

import java.io.Reader;
import java.util.LinkedList;

public class RequestQueue implements Runnable{
	private LinkedList< Request > m_pendingRequestList = new LinkedList< Request >();
	private LinkedList< Response > m_completedResponseList = new LinkedList< Response >();
	private Thread m_thread = null;
	
	public void sendRequest( Request request )
	{
		m_pendingRequestList.add( request );
		if( m_thread == null ||
			!m_thread.isAlive())
		{
			m_thread = new Thread(this);
			m_thread.start();
		}
	}
	
	public void run()
	{
		while( !m_pendingRequestList.isEmpty() )
		{
			synchronized (this) {
				Request request = m_pendingRequestList.removeFirst();
				Response response = request.execute();
				m_completedResponseList.add( response );
				notifyAll();				
			}
		}
	}
	
	public Response waitForRequestResponse( int id )
	{
		while( true )
		{
			synchronized (this) {
				for( Response response: m_completedResponseList )
				{
					if( response.id() == id )
					{
						return response;
					}
				}
				try {
					wait();					
				} catch (InterruptedException e) {
					//ignore interrupts 
				}

			}
		}
	}
}
