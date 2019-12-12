package ca.redleafsolutions.ishell2.ui.notifications.javaws;

import java.net.InetAddress;
import java.nio.channels.ClosedByInterruptException;

import org.java_websocket.WebSocket;

import ca.redleafsolutions.Trace;
import ca.redleafsolutions.base.events.EventDispatcher;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.ui.notifications.ChannelEvent;
import ca.redleafsolutions.ishell2.ui.notifications.WebSocketConnection;
import ca.redleafsolutions.ishell2.ui.notifications.WebSocketMessage;

public class JavaWebSocketConnection extends EventDispatcher<ChannelEvent> implements WebSocketConnection {
	private WebSocket conn;
	private long timeOpened;
	private long timeSent;
	private long timeReceived;

	public JavaWebSocketConnection(WebSocket conn) {
		this.conn = conn;
		timeOpened = System.currentTimeMillis ();
	}
	
	@Override
	public void send(String message) {
		conn.send(message);
		timeSent = System.currentTimeMillis ();
	}

	@Override
	public void close(int code) {
		conn.close(code);
		dispatchEvent (new ChannelEvent.WebSocketDisconnected (this));
	}

	@Override
	public InetAddress remote () {
		return conn.getRemoteSocketAddress ().getAddress ();
	}

	@Override
	public void handleMessage (WebSocketMessage message) {
		timeReceived = System.currentTimeMillis ();
		dispatchEvent (new ChannelEvent.MessageReceived (this, message));
	}

	@Override
	public void handleError (Throwable e) {
		Trace.info ("Notification channel " + remote () + " error " + e);
		iLogger.warning ("Notification channel " + remote () + " error " + e);
		if (e instanceof ClosedByInterruptException) {
			close (0);
		}
	}
	
	public String stats () {
		String s = "";
		if (conn.isOpen ()) {
			s += "Opened " + ((System.currentTimeMillis () - timeOpened)/1000) + " sec ago" + iShell.lineSeparater;
			s += "Sent " + ((System.currentTimeMillis () - timeSent)/1000) + " sec ago" + iShell.lineSeparater;
			s += "Received " + ((System.currentTimeMillis () - timeReceived)/1000) + " sec ago" + iShell.lineSeparater;
		} else {
			s += "Closed";
		}
		return s;
	}

	@Override
	public String toString () {
		return remote ().getHostAddress () + "/" + this.hashCode ();
	}
	
	@Override
	public boolean isOpen () {
		return conn.isOpen ();
	}
}
