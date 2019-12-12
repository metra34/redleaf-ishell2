package ca.redleafsolutions.ishell2.ui.notifications.javaws;

import ca.redleafsolutions.ishell2.ui.notifications.WebSocketMessage;

public class JavaWebSocketMessage implements WebSocketMessage {
	private String message;

	public JavaWebSocketMessage (String message) {
		this.message = message;
	}

	@Override
	public String toString () {
		return message;
	}
}
