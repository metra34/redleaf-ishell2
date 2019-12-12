package ca.redleafsolutions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.SystemUtils;

public class CommandLineRunner {
	public static CommandLineRunner create (String... cmdstr) throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			return new Windows (cmdstr);
		}
		if (SystemUtils.IS_OS_LINUX) {
			return new Linux (cmdstr);
		}
		throw new Exception ("System type does not support command line execution");
	}

	private String[] cmdstr;

	public CommandLineRunner (String... cmdstr) {
		this.cmdstr = cmdstr;
	}

	public Process exec () throws IOException {
		return Runtime.getRuntime ().exec (cmdstr);
	}

	public ObjectMap execAndComplete () throws IOException {
		ObjectMap map = new ObjectMap ();
		long tic = System.nanoTime ();
		Process proc = exec ();
		BufferedReader output = new BufferedReader (new InputStreamReader (proc.getInputStream ()));
		BufferedReader err = new BufferedReader (new InputStreamReader (proc.getErrorStream ()));
		while (proc.isAlive ()) {
		}

		String stdout = "", stderr = "", line;
		while ((line = output.readLine ()) != null) {
			stdout += line + System.lineSeparator ();
		}
		while ((line = err.readLine ()) != null) {
			stderr += line + System.lineSeparator ();
		}
		map.put ("stdout", stdout);
		map.put ("stderr", stderr);
		map.put ("command", cmdstr);
		map.put ("duration", System.nanoTime () - tic);
		map.put ("exit-code", proc.exitValue ());
		return map;
	}

	static public class Windows extends CommandLineRunner {
		public Windows (String... cmdstr) {
			this (cmdstr[0]);
		}
		public Windows (String cmdstr) {
			super ("cmd /c start " + cmdstr);
		}
	}

	static public class Linux extends CommandLineRunner {
		public Linux (String... cmdstr) {
//			super ("/bin/bash -c " + cmdstr + "");
			super (cmdstr);
		}
	}
}
