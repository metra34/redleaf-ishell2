package ca.redleafsolutions.ishell2;

public class IShellRequestScript extends IShellRequest {
	private String script;

	public IShellRequestScript (String script) {
		this.script = script;
	}

	public String getScript () {
		return script;
	}
}
