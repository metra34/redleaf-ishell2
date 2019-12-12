package ca.redleafsolutions.ishell2.ui.notifications;

@SuppressWarnings ("serial")
public class NotificationChannelException extends Exception {
	public static class MissingParameters extends NotificationChannelException{
		private String urlquery;

		public MissingParameters(String urlquery) {
			this.urlquery = urlquery;
		}
		
		@Override
		public String toString() {
			return super.toString() + ": " + urlquery;
		}
	}
	
	public static class SessionNotFound extends Exception {
		private String sessionid;

		public SessionNotFound (String sessionid) {
			this.sessionid = sessionid;
		}
		
		public String getSessionID () {
			return sessionid;
		}
	}
}
