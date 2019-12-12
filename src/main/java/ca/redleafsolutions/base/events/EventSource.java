package ca.redleafsolutions.base.events;

/** An interface definition of an event generating object */
public interface EventSource<T extends Event> {

	/** Add an event handler to handle events of type 'eventType' event.
	 * @param handler the event handler class to call in case of matching event
	 */
	public void addEventHandler (EventHandler<T> handler);

	/** Remove an event handler from handler list
	 * @param handler the event handler class to remove
	 */
	public void removeEventHandler (EventHandler<T> handler);
	
	/** Dispatch the incoming event to all registered handlers.
	 * event, message and signaling.
	 * @param event the incoming event
	 */
	public void dispatchEvent (T event);

	/** How many handlers are registered
	 * @return number of handlers registered
	 */
	int numHandlers ();
}
