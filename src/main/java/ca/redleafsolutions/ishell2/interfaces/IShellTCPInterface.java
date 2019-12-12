/*
* iShell 2.0
*
* Copyright (c) 2010, Redleaf Solutions Ltd. All rights reserved.
*
* This library is proprietary software; you can not redistribute
* without an explicit consent from Releaf Solutions Ltd.
* The consent will detail the distribution and sale rights.
*/

package ca.redleafsolutions.ishell2.interfaces;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import ca.redleafsolutions.Trace;
import ca.redleafsolutions.base.events.EventHandler;
import ca.redleafsolutions.ishell2.iShell;
import ca.redleafsolutions.json.JSONItem;
import ca.redleafsolutions.json.JSONValidationException;

public class IShellTCPInterface extends IShellInterfaceImpl implements Runnable, EventHandler<WorkerEvent> {
	protected int port;
	protected String prompt;
	private Thread thread;
	private Set<SocketWorker> connections;

	public IShellTCPInterface(iShell main, JSONItem params) throws JSONValidationException, IOException {
		super(main, params);

		String sport = null;
		try {
			port = params.getInt("port");
		} catch (JSONValidationException e) {
			sport = params.getString("port");
			if ("random".equalsIgnoreCase(sport)) {
				port = (int) (Math.random() * (0xFFFF - 2000) + 2000);
			} else {
				port = Integer.parseInt(sport);
			}
		}

		try {
			prompt = params.getString ("prompt");
		} catch (JSONValidationException.MissingKey e) {
			prompt = "";
		}
		
		connections = new HashSet<>();
		thread = new Thread(this);
		thread.start();
	}

	public Set<SocketWorker> connections () {
		return connections;
	}
	
	@Override
	protected String getNativeFormat() {
		return "TEXT";
	}

	@Override
	public boolean isBrowserInterface() {
		return false;
	}

	@Override
	public void run() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			Trace.info("TCP Socket opened on port " + port);
			while (true) {
				Socket socket;
				try {
					socket = server.accept();
					SocketWorker worker;
					if ((prompt != null) && !prompt.isEmpty ()) {
						worker = new SocketWorker(this, socket, prompt);
					} else {
						worker = new SocketWorker(this, socket);
					}
					worker.addEventHandler(this);
					connections.add(worker);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void handleEvent(WorkerEvent event) {
		if (event instanceof WorkerEvent.StreamClosed) {
			connections.remove(((WorkerEvent.StreamClosed) event).getSocketWorker());
		}
	}

	@Override
	public String info() {
		return "TCP Server on port " + port;
	}
}
