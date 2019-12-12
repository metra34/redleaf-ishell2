package ca.redleafsolutions.ishell2.interfaces;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SocketWorker extends StreamWorker {
	private Socket socket;

	public SocketWorker(IShellInterfaceImpl iface, Socket socket) throws IOException {
		super (iface, socket.getInputStream(), new PrintStream(socket.getOutputStream()), "TCP-" + socket.getRemoteSocketAddress());
		this.socket = socket;
	}
	
	public SocketWorker(IShellInterfaceImpl iface, Socket socket, String prompt) throws IOException {
		super (iface, socket.getInputStream(), new PrintStream(socket.getOutputStream()), "TCP-" + socket.getRemoteSocketAddress(), prompt);
		this.socket = socket;
	}
	
	@Override
	protected void close() throws IOException {
		socket.close();
	}
}
