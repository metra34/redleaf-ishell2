package ca.redleafsolutions.base.events;

/** A base definition of any class that handles events of type T */
public interface EventHandler<T extends Event> {
	/** This is the place holder for all event handling procedures.
	 * @param event the event that just occurred
	 */
	public void handleEvent (T event);
}
