package ca.redleafsolutions.ishell2.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import ca.redleafsolutions.base.events.EventDispatcher;
import ca.redleafsolutions.ishell2.IShellRequestScript;
import ca.redleafsolutions.ishell2.IShellRequestSingle;
import ca.redleafsolutions.ishell2.IShellResponse;
import ca.redleafsolutions.ishell2.ParseRequestResults;
import ca.redleafsolutions.ishell2.logs.iLogger;

public class StreamWorker extends EventDispatcher<WorkerEvent> implements Runnable {
	private Thread thread;
	private IShellInterfaceImpl iface;
	private InputStream is;
	private PrintStream os;
	private String prompt;

	public StreamWorker(IShellInterfaceImpl iface, InputStream is, PrintStream os, String threadName) {
		this (iface, is, os, threadName, null);
	}
	public StreamWorker(IShellInterfaceImpl iface, InputStream is, PrintStream os, String threadName, String prompt) {
		this.iface = iface;
		this.is = is;
		this.os = os;
		this.prompt = prompt;

		thread = new Thread(this, threadName);
		thread.start();
	}

	@Override
	public void run() {
		try {
			StringBuffer multiline = null;
			byte[] buff = new byte[1024];
			while (true) {
				if (prompt != null) {
					os.print(prompt);
				}
				int len = is.read(buff);
				String line = new String(buff, 0, len);
				line = _rtrim(line);
				if (line.length() > 0) {
					if ("`".equals(line)) {
						if (multiline == null) {
							multiline = new StringBuffer();
						} else {
							try {
								IShellRequestScript request = new IShellRequestScript(multiline.toString());
								IShellResponse response = iface.executeAndRespond(request);
								String s = response.toString();
								s = s.trim();
								if (s.length() > 0)
									os.println(s);
							} finally {
								multiline = null;
							}
						}
					} else {
						if (multiline == null) {
							line = line.trim();
							ParseRequestResults parsed = new ParseRequestResults(line);

							IShellRequestSingle request = new IShellRequestSingle(parsed);
							int reqid = iLogger.logIShellRequest(request);

							IShellResponse response = iface.executeAndRespond(request, parsed);

							// String s = generateOutput (parsed.getOutputFormat (),
							// response);
							String s = response.toString();
							s = s.trim();
							if (s.length() > 0)
								os.println(s);
							iLogger.logIShellResponse(reqid, s);
						} else {
							if (multiline.length() > 0)
								multiline.append(System.lineSeparator());
							multiline.append(line);
						}
					}
				}
			}
		} catch (Throwable e) {
			try {
				close ();
			} catch (IOException e1) {
				iLogger.warning("Failed to close interface stream " + this);
			}
			dispatchEvent(new WorkerEvent.StreamClosed (this));
		}
	}

	protected void close () throws IOException {
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		os.close();
	}
	
	private String _rtrim(String s) {
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}
}
