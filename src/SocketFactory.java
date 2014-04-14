import java.rmi.server.*;
import java.io.*;
import java.net.*;

public class SocketFactory extends RMISocketFactory implements Serializable {
	
	private static final long serialVersionUID = 2130625001975837743L;
	
	private InetAddress ipInterface = null;

	public SocketFactory() {
	}

	public SocketFactory(InetAddress ipInterface) {
		this.ipInterface = ipInterface;
	}

	public ServerSocket createServerSocket(int port) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, 50, ipInterface);
		} catch (Exception e) {
			System.out.println(e);
		}
		return (serverSocket);
	}

	public Socket createSocket(String dummy, int port) throws IOException {
		return (new Socket(ipInterface, port));
	}

	public boolean equals(Object that) {
		return (that != null && that.getClass() == this.getClass());
	}
}
