package ca.redleafsolutions.ishell2.ui.notifications;

import java.io.IOException;
import java.net.InetAddress;

import ca.redleafsolutions.base.events.EventSource;

public interface WebSocketConnection extends EventSource<ChannelEvent> 	{
	public void send (String message) throws IOException;

	public void handleMessage (WebSocketMessage message);

	public void close (int code);

	public InetAddress remote ();

	public void handleError (Throwable e);
	
	public boolean isOpen ();
}
