package ca.redleafsolutions;

public class SingletonException extends Exception {
	private static final long serialVersionUID = 3625981522178554971L;

	public SingletonException (Object o) {
		super ("Class " + o.getClass ().getSimpleName () + " was already isntantiated. Only one permitted (Singleton)");
	}

}
