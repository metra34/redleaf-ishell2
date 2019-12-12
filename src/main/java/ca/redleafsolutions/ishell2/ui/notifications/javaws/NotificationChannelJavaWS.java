package ca.redleafsolutions.ishell2.ui.notifications.javaws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ca.redleafsolutions.StringMap;
import ca.redleafsolutions.ishell2.IShellException;
import ca.redleafsolutions.ishell2.IShellException.AlreadyExists;
import ca.redleafsolutions.ishell2.logs.iLogger;
import ca.redleafsolutions.ishell2.ui.notifications.ChannelEvent;
import ca.redleafsolutions.ishell2.ui.notifications.NoticiationChannelBase;
import ca.redleafsolutions.ishell2.ui.notifications.WebSocketConnection;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;
import ca.redleafsolutions.json.JSONValidationException.MissingKey;

public class NotificationChannelJavaWS extends NoticiationChannelBase {
	private WebSocketServer websockserver;
	private int port;

	public NotificationChannelJavaWS () throws MissingKey {
		super ();
	}

	public NotificationChannelJavaWS (JSONItem json) throws MissingKey {
		this ();
		try {
			this.port = json.getInt ("port");
		} catch (JSONValidationException e) {
			this.port = -1;
		}
	}

	@Override
	public boolean isRunning () {
		return websockserver != null;
	}

	@Override
	public int port () {
		if (websockserver == null) {
			if (port < 0) {
				int counter = 100;
				while (--counter > 0) {
					try {
						server ((int)(Math.random () * (0xFFFF - 0x2000) + 0x2000));
						counter = 0;
					} catch (AlreadyExists e) {
						// Should never happen
						iLogger.warning ("Failed to open WebSocket. Attempt " + (100 - counter));
					}
				}
			} else {
				try {
					server (port);
				} catch (AlreadyExists e) {
					// Should never happen
					iLogger.severe (e);
				}
			}
		}
		return websockserver.getPort ();
	}

	@Override
	public int secureport () {
		// the renderer will not add ssl param if port is negative
		return -1;
	}

	public void server (int port) throws AlreadyExists {
		if (websockserver != null)
			throw new IShellException.AlreadyExists ();

		//		sessions = new HashMap<String, Collection<WebSocketWrapper>> ();

		InetSocketAddress addr = new InetSocketAddress (port);
		List<Draft> drafts = new LinkedList<Draft> ();
		// drafts.add (new Draft_10 ());
		drafts.add (new Draft_17 ()); // TODO learn what drafts should be
		// included
		// drafts.add (new Draft_75 ());
		// drafts.add (new Draft_76 ());

		websockserver = new IShellWebSocketServer (this, addr, drafts);
		websockserver.start ();
	}

	public void stopserver () throws IOException, InterruptedException {
		if (websockserver == null)
			return;

		Collection<WebSocket> con = websockserver.connections ();
		synchronized (con) {
			for (WebSocket c:con) {
				c.close (0);
			}
		}

		websockserver.stop ();
		websockserver = null;
	}

	public void restart () throws IOException, InterruptedException, AlreadyExists {
		try {
			if (websockserver.connections ().size () <= 0) {
				forceRestart ();
				return;
			}
		} catch (Exception e) {
		}
		throw new InterruptedException ("Not allowed to restart when connections are open. Use 'forceRestart'");
	}

	public void forceRestart () throws IOException, InterruptedException, AlreadyExists {
		stopserver ();
		port ();
	}

	public boolean checkport () {
		int port = websockserver.getPort ();
		try {
			ServerSocket ssock = new ServerSocket (port);
			ssock.close ();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private class IShellWebSocketServer extends WebSocketServer {
		//		private NotificationChannelJavaWS channel;
		private Map<WebSocket, WebSocketConnection> sockets;
		private NotificationChannelJavaWS channel;

		public IShellWebSocketServer (NotificationChannelJavaWS notificationChannel, InetSocketAddress addr,
				List<Draft> drafts) {
			super (addr, drafts);
			this.channel = notificationChannel;
			//			this.channel = notificationChannel;
			sockets = new HashMap<WebSocket, WebSocketConnection> ();
		}

		@Override
		public void onOpen (WebSocket conn, ClientHandshake handshake) {
			URI uri = URI.create (handshake.getResourceDescriptor ());

			iLogger.info ("Notification channel connection request from " + handshake.getResourceDescriptor ());

			String query = uri.getRawQuery ();
			StringMap map = new StringMap ();
			map.fromSearchString (query);

			JavaWebSocketConnection connection = new JavaWebSocketConnection (conn);
			connection.addEventHandler (channel);
			sockets.put (conn, connection);
			channel.handleEvent (new ChannelEvent.WebSocketConnected (connection, map));
		}

		@Override
		public void onMessage (WebSocket conn, String message) {
			WebSocketConnection connection = sockets.get (conn);
			if (connection != null)
				connection.handleMessage (new JavaWebSocketMessage (message));
		}

		@Override
		public void onError (WebSocket conn, Exception ex) {
			WebSocketConnection connection = sockets.get (conn);
			if (connection != null)
				connection.handleError (ex);
			if (conn.isClosed ()) {
				sockets.remove (conn);
				channel.handleEvent (new ChannelEvent.WebSocketDisconnected (connection));
			}
		}

		@Override
		public void onClose (WebSocket conn, int code, String reason, boolean remote) {
			iLogger.info ("Closing notification channel. Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
			WebSocketConnection connection = sockets.get (conn);
			if (connection != null)
				connection.close (0);
			if (conn.isClosed ()) {
				sockets.remove (conn);
				channel.handleEvent (new ChannelEvent.WebSocketDisconnected (connection));
			}
		}
	}

	@Override
	public String type () {
		return "JavaWebSocket";
	}
}
