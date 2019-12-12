package ca.redleafsolutions.ishell2.ui.notifications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import ca.redleafsolutions.BaseList;
import ca.redleafsolutions.ObjectMap;
import ca.redleafsolutions.ResourceLocator;
import ca.redleafsolutions.TemplateUtils;
import ca.redleafsolutions.Trace;
import ca.redleafsolutions.base.events.EventDispatcher;
import ca.redleafsolutions.base.events.EventHandler;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.ishell2.annotations.IShellInvisible;
import ca.redleafsolutions.ishell2.annotations.MethodDescription;
import ca.redleafsolutions.ishell2.annotations.ParameterDescriptions;
import ca.redleafsolutions.ishell2.annotations.ParameterNames;
import ca.redleafsolutions.json.JSONWritable;

public abstract class NoticiationChannelBase extends EventDispatcher<ChannelEvent> implements EventHandler<ChannelEvent> {
	protected BaseList<WebSocketConnection> connections;

	public NoticiationChannelBase () {
		connections = new BaseList<> ();
	}

	@MethodDescription ("Get listening port")
	public abstract int port ();
	public abstract int secureport ();
	public abstract boolean isRunning ();
	public abstract String type ();

	public ObjectMap info () {
		ObjectMap map = new ObjectMap ();
		map.put ("port", port ());
		map.put ("running", isRunning ());
		if (secureport () > 0) {
			map.put ("secure", true);
			map.put ("secure", secureport ());
		} else {
			map.put ("secure", false);
		}
		map.put ("connections", connections);
		return map;
	}

	@MethodDescription ("Get the JavaScript file that implements browser side channel")
	public String js () throws FileNotFoundException {
		ResourceLocator locator = new ResourceLocator (NoticiationChannelBase.class);
		InputStream is = locator.getInputStream ("wschannel.js");
		if (is == null) {
			throw new FileNotFoundException ("wschannel.js");
		}
		ObjectMap map = new ObjectMap ();
		map.put ("ishell", iShell.getInstance ());
		map.put ("channel", this);
		try {
			return TemplateUtils.evaluate (is, map);
		} finally {
			if (is != null)
				try {
					is.close ();
				} catch (IOException e) {
					// do nothing
				}
		}
	}

	@IShellInvisible
	public void broadcast (JSONWritable data) throws IOException {
		broadcast (data.toString ());
	}
	@MethodDescription ("Send text to all open channels")
	@ParameterNames ("data")
	@ParameterDescriptions ("The data to send to all open channels")
	public void broadcast (String str) throws IOException {
		for (Iterator<WebSocketConnection> it=connections.iterator (); it.hasNext (); ) {
			WebSocketConnection connection = it.next ();
			if (connection.isOpen ()) {
				connection.send (str);
			} else {
				it.remove ();
			}
		}
	}
	
	@MethodDescription ("Get all open connections")
	public BaseList<WebSocketConnection> connections () {
		return connections;
	}

	@Override
	public void handleEvent (ChannelEvent event) {
		if (event instanceof ChannelEvent.WebSocketConnected) {
			ChannelEvent.WebSocketConnected ev = (ChannelEvent.WebSocketConnected)event;
			connections.add (ev.getConnection ());
			Trace.info ("WebSocket Connected from " + ev.getConnection ().remote ());
			dispatchEvent (event);
		} else if (event instanceof ChannelEvent.WebSocketDisconnected) {
			ChannelEvent.WebSocketDisconnected ev = (ChannelEvent.WebSocketDisconnected)event;
			connections.remove (ev.getConnection ());
			Trace.info ("WebSocket Disconnected");
			dispatchEvent (event);
		} else if (event instanceof ChannelEvent.MessageReceived) {
			Trace.info ("Received '" + ((ChannelEvent.MessageReceived)event).getMessage () + "' from " + ((ChannelEvent.MessageReceived)event).getConnection ());
			dispatchEvent (event);
		} else if (event instanceof ChannelEvent.MessageSent) {
			Trace.info ("Sent '" + ((ChannelEvent.MessageReceived)event).getMessage () + "' to " + ((ChannelEvent.MessageReceived)event).getConnection ());
			dispatchEvent (event);
		}
	}
}
