package ca.redleafsolutions.ishell2.interfaces;

import ca.redleafsolutions.base.events.Event;

public class WorkerEvent implements Event {
	public static class StreamClosed extends WorkerEvent {
		private StreamWorker worker;

		public StreamClosed(StreamWorker worker) {
			this.worker = worker;
		}
		
		public StreamWorker getSocketWorker() {
			return worker;
		}
	}
}
