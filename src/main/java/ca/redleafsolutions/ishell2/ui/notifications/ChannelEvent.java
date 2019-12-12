package ca.redleafsolutions.ishell2.ui.notifications;

import ca.redleafsolutions.StringMap;
import ca.redleafsolutions.base.events.Event;

public interface ChannelEvent extends Event {
	public static class WebSocketConnected implements ChannelEvent {
		private StringMap map;
		private WebSocketConnection connection;

		public WebSocketConnected (WebSocketConnection connection, StringMap map) {
			this.connection = connection;
			this.map = map;
		}
		
		public StringMap getParams () {
			return map;
		}
		
		public WebSocketConnection getConnection () {
			return connection;
		}
	}

	public static class WebSocketDisconnected implements ChannelEvent {
		private WebSocketConnection connection;

		public WebSocketDisconnected (WebSocketConnection connection) {
			this.connection = connection;
		}
		
		public WebSocketConnection getConnection () {
			return connection;
		}
	}

	public static abstract class _Message implements ChannelEvent {
		protected WebSocketConnection connection;
		protected WebSocketMessage message;

		protected _Message (WebSocketConnection connection, WebSocketMessage message) {
			this.connection = connection;
			this.message = message;
		}
		
		public WebSocketMessage getMessage () {
			return message;
		}
		
		public WebSocketConnection getConnection () {
			return connection;
		}
	}

	public static class MessageReceived extends _Message {
		public MessageReceived (WebSocketConnection connection, WebSocketMessage message) {
			super (connection, message);
		}
	}

	public static class MessageSent extends _Message {
		public MessageSent (WebSocketConnection connection, WebSocketMessage message) {
			super (connection, message);
		}
	}
}
