package ca.redleafsolutions.ishell2.interfaces;

import java.io.IOException;

public class ConsoleWorker extends StreamWorker {
	public ConsoleWorker(IShellInterfaceImpl iface) {
		super (iface, System.in, System.out, "Console", "> ");
	}
	
	@Override
	protected void close() throws IOException {
		// do nothing
	}
}
