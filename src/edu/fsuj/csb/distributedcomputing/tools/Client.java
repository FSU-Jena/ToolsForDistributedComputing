package edu.fsuj.csb.distributedcomputing.tools;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * implements the client handle of a server-client connection
 * @author Stephan Richter
 *
 */
public class Client {
	protected Socket socket;
	private ObjectOutputStream objectOutStream=null;
	
	/**
	 * super method, that is called, when the client side recieves an object from the server. shall be overridden by extending classes
	 * @param o the object recieved
	 * @throws IOException
	 */
	public void handleObject(Object o) throws IOException {
		System.out.println("recieved " + o);
		sendObject("Recieved an Object!");
	}

	/**
	 * sends an object to the server endpoint of the connection
	 * @param o the object to be sent
	 * @throws IOException
	 */
	public void sendObject(Serializable o) throws IOException {
		OutputStream outStream = socket.getOutputStream();
		if (objectOutStream==null) objectOutStream = new ObjectOutputStream(outStream);
		objectOutStream.writeObject(o);
  }
	
	/**
	 * this thread listens on the server client connection for incoming objects
	 * @author Stephan Richter
	 *
	 */
	private class ListenThread extends Thread {
		private ObjectInputStream ois;

		/**
		 * creates a new listen thread
		 * @param s the socket, which is used by the connection
		 * @throws IOException
		 * @throws ClassNotFoundException
		 */
		public ListenThread(Socket s) throws IOException, ClassNotFoundException {
			socket = s;
			ois = new ObjectInputStream(socket.getInputStream());
			System.out.println("connected at port " + socket.getPort());
		}

		/**
		 * listens for objects on the connection
		 */
		public void run() {
			if (socket != null) {
				try {
					while (true) {
						Object o = ois.readObject();
						if (o instanceof Signal) {
							System.out.println("recieved "+o);
							Signal sig = (Signal) o;
							if (sig.type() == Signal.KILL) break;
						} else handleObject(o);
					}
					ois.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			System.out.println("Closing connection.");
		}
	}

	/**
	 * creates a new client by establishin a server connection
	 * @param hostname the hostname of the server
	 * @param port the port to connecto to
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public Client(String hostname, int port) throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		Socket socket = connect(hostname,port);
		(new ListenThread(socket)).start();
		Thread.sleep(10);
	}

	/**
	 * creates a new client by establishing a server connection on the default port
	 * @param hostname the hostname of the server
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public Client(String hostname) throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		this(hostname,Ports.registrationPort());
  }

	/**
	 * @param max the maximum of the numbers to be generated
	 * @return a random number between 0 and max
	 */
	private static int random(int max) {
		return (int) (max * Math.random());
	}

	/**
	 * tries to connect the client to the server
	 * @param hostname the hostname of the server
	 * @param port the port at the server, which shall be used
	 * @return the connection socket
	 * @throws IOException
	 */
	public static Socket connect(String hostname, int port) throws IOException {
		Socket result = null;
		while (result == null) {
			try {
					result = new Socket(hostname, port);				
			} catch (ConnectException e) {
				if (e.getMessage().equals("Connection refused")){
					try {
	          Thread.sleep(10+random(1000)); // if server refused connection: wait a random period of time and try again
          } catch (InterruptedException e1) {}
				} else break;
			}
		}
		return result;
	}

	/**
	 * a short test program
	 * @param args
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		new Client("localhost",Ports.registrationPort());
	}
}
