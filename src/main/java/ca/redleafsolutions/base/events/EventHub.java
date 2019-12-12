package ca.redleafsolutions.base.events;

import java.util.HashSet;
import java.util.Set;

/**
 * The handler list is an implementation of an event hub. Any instance can
 * register on any event and any instance can dispatch any event without
 * registration
 */
public class EventHub<E extends Event> {
	private Set<EventHandler<E>> eventHandlers;
	
	/** Singleton pattern: will instantiate after first address to getInstance method */
	static volatile private EventHub<? extends Event> instance = null;
	static public EventHub<? extends Event> getInstance () {
		if (instance == null) {
			instance = new EventHub<> ();
		}
		return instance;
	}

	private EventHub () {
		eventHandlers = new HashSet<EventHandler<E>> ();
	}
	
	/**
	 * Add an event handler to handle events of type 'eventType' event.
	 *
	 * @param handler
	 *            the event handler class to call in case of matching event
	 */
	public void addEventHandler (EventHandler<E> handler) {
		if (handler == null)
			return;
		synchronized (eventHandlers) {
			eventHandlers.add (handler);
		}
	}

	/**
	 * Remove an event handler from handler list
	 * 
	 * @param handler
	 *            the event handler class to remove
	 */
	public void removeEventHandler (EventHandler<E> handler) {
		if (handler == null)
			return;
		synchronized (eventHandlers) {
			eventHandlers.remove (handler);
		}
	}

	/**
	 * Dispatch an event into the hub to be picked up by handlers.
	 * 
	 * @param source
	 *            the dispatcher object
	 * @param event
	 *            the incoming event
	 */
	public void dispatchEvent (Object source, E event) {
		try {
			synchronized (eventHandlers) {
				for (EventHandler<E> handler: eventHandlers) {
					handler.handleEvent (event);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		}
	}
}
