package ca.redleafsolutions.base.events;

import java.util.LinkedList;
import java.util.List;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;

/**
 * The handler list is an implementation of an event dispatcher. Other consumers
 * can subscribe for events and the handler list will dispatch the event to all
 * subscribers
 */
public class EventDispatcher<T extends Event> implements EventSource<T> {
	private List<EventHandler<T>> eventHandlers = new LinkedList<EventHandler<T>> ();
	private int refcounter = 0;

	/**
	 * Add an event handler to handle events of type 'eventType' event.
	 * @param handler the event handler class to call in case of matching event
	 */
	public void addEventHandler (EventHandler<T> handler) {
		if (handler == null)
			return;
		if (eventHandlers.contains (handler))
			return;

		if (refcounter > 0) {
			HandlerThreader.addHandler (eventHandlers, handler);
		} else {
			eventHandlers.add (handler);
//			Trace.info ("Added " + handler.getClass ().getSimpleName () + " as handler to " + this.getClass ().getSimpleName () + " (" + eventHandlers +")");
		}
	}

	/**
	 * Remove an event handler from handler list
	 * 
	 * @param handler
	 *            the event handler class to remove
	 */
	public void removeEventHandler (EventHandler<T> handler) {
		if (handler == null)
			return;

		synchronized (eventHandlers) {
			if (!eventHandlers.contains (handler))
				return;

			if (refcounter > 0) {
				EventDispatcher.HandlerThreader.removeHandler (eventHandlers, handler);
			} else {
//				int len = eventHandlers.size ();
				eventHandlers.remove (handler);
//				Trace.info ("removing " + handler.getClass ().getSimpleName () + " from " + this.getClass ().getSimpleName () + " (" + len + " -> " + eventHandlers.size () + ")");
			}
		}
	}

	/** @see EventDispatcher */
	@IShellInvisible
	public int numHandlers () {
		return eventHandlers.size ();
	}
	
	/**
	 * Dispatch the incoming event to all registered handlers. event, message
	 * and signaling.
	 * 
	 * @param event
	 *            the imcoming event
	 */
	public void dispatchEvent (T event) {
//		Trace.info ("Dispatching " + event.getClass ().getSimpleName () + " event to " + eventHandlers);
		if (eventHandlers.size () <= 0)
			return;
		++refcounter;
		try {
			synchronized (eventHandlers) {
				for (EventHandler<T> handler: eventHandlers) {
					handler.handleEvent (event);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} finally {
			--refcounter;
		}
	}

	/** handle concurrent modifications to handler lists */
	static abstract private class HandlerThreader<T extends Event> extends Thread {
		static <T extends Event> void addHandler (List<EventHandler<T>> eventHandlers, EventHandler<T> handler) {
			new HandlerAdder<T> (eventHandlers, handler).start ();
		}

		static <T extends Event> void removeHandler (List<EventHandler<T>> eventHandlers, EventHandler<T> handler) {
			new HandlerRemover<T> (eventHandlers, handler).start ();
		}

		protected List<EventHandler<T>> eventHandlers;
		protected EventHandler<T> handler;

		private HandlerThreader (List<EventHandler<T>> eventHandlers, EventHandler<T> handler) {
			super ("Handler Adder");
			this.eventHandlers = eventHandlers;
			this.handler = handler;
		}

		static private class HandlerAdder<T extends Event> extends HandlerThreader<T> {
			protected HandlerAdder (List<EventHandler<T>> eventHandlers, EventHandler<T> handler) {
				super (eventHandlers, handler);
			}

			@Override
			public void run () {
				synchronized (eventHandlers) {
					eventHandlers.add (handler);
				}
			}
		}

		static private class HandlerRemover<T extends Event> extends HandlerThreader<T> {
			protected HandlerRemover (List<EventHandler<T>> eventHandlers, EventHandler<T> handler) {
				super (eventHandlers, handler);
			}

			@Override
			public void run () {
				synchronized (eventHandlers) {
					eventHandlers.remove (handler);
				}
			}
		}
	}
}
