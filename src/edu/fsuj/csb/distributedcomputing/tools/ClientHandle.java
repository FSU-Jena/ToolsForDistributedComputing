package edu.fsuj.csb.distributedcomputing.tools;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;


/**
 * implements the client side handle of a server-client connection
 * @author Stephan Richter
 *
 */
public class ClientHandle {
	
	private Socket sock;
	private ObjectOutputStream objectOutStream;
	private static int number=0;
	private int myNumber;
	
	/**
	 * create new client handle on given socket
	 * @param s the socket used
	 * @throws IOException
	 */
	public ClientHandle(Socket s) throws IOException {
		myNumber=++number;		
		sock=s;
		OutputStream outStream = sock.getOutputStream();  
		objectOutStream = new ObjectOutputStream(outStream);
  }

	/**
	 * send a serializable object to the server side
	 * @param o the object to send
	 * @throws IOException
	 */
	public void send(Serializable o) throws IOException {
		System.out.print("Sending " + ((o instanceof Signal)?o:o.getClass().getSimpleName()) + " to client...");		
		objectOutStream.writeObject(o);
		System.out.println("done.");
  }

	/**
	 * @return the port on which the client is connected to the server
	 */
	public int port() {
	  return sock.getLocalPort();
  }

	/**
	 * close the server-client connection
	 * @throws IOException
	 */
	public void close() throws IOException {
		send(new Signal(Signal.KILL));
		sock.close();
  }

	/**
	 * request the objectInputStram of the client handle
	 * @return the object input stream of the client handle
	 * @throws IOException
	 */
	public ObjectInputStream objectInputStream() throws IOException {
		InputStream is = sock.getInputStream();
		return new ObjectInputStream(is);
	}
	
	/**
	 * @return the (unique) number of this handle
	 */
	public int number(){
		return myNumber;
	}
}
