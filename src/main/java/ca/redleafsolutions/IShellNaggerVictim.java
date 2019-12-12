package ca.redleafsolutions;

import ca.redleafsolutions.ishell2.annotations.IShellInvisible;

public interface IShellNaggerVictim extends NaggerVictim {
	@IShellInvisible
	void again ();
	@IShellInvisible
	void waitCondition ();
	@IShellInvisible
	boolean done ();
	@IShellInvisible
	void handleNaggerException (Throwable e);
}
