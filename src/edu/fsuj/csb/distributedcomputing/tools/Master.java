package edu.fsuj.csb.distributedcomputing.tools;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.TreeSet;

import edu.fsuj.csb.tools.xml.ObjectComparator;

/**
 * this class manages the server side of server-client connections
 * @author Stephan Richter
 *
 */
public class Master {
	private TreeSet<ClientHandle> idleHandles; // stores the client handles of idle clients
	private TreeSet<ClientHandle> workingClients; // stores the client handles of working clients

	/**
	 * adds a new client (which is expected to be idle)
	 * @param client the client handle
	 */
	private synchronized void addClientHandle(ClientHandle client) {
		System.out.println("Client ("+client.number()+") connected at port " + client.port());
		idleHandles.add(client);
	}
	
	/**
	 * generic method for handling recieved objects
	 * @param o
	 */
	public void handleObject(Object o) {
		System.out.println("recieved " + o);
	}

	/**
	 * implements a thread, that listenes for object submitted by the connected client
	 * @author Stephan Richter
	 *
	 */
	private class ClientThread extends Thread {
		private Socket client;

		public ClientThread(Socket client) {
			this.client = client;
		}

		public void run() {
			try {
				ClientHandle cH=new ClientHandle(client);
				addClientHandle(cH);
				ObjectInputStream ois = cH.objectInputStream();
				while (true) {
					Object o = ois.readObject();
					if (o instanceof Signal) {
						Signal sig = (Signal) o;
						if (sig.type() == Signal.KILL) {
							break;
						} else if (sig.type()==Signal.IDLE){
							String handleDesc=cH.toString();
							System.out.print("Adding "+handleDesc.substring(handleDesc.lastIndexOf(".")+1)+" to idle clients...");
							workingClients.remove(cH);
							idleHandles.add(cH);
							System.out.println("done.");
						}
					} else handleObject(o);
				}
				ois.close();
				cH.close();
			} catch (SocketException e){
			} catch (EOFException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
	      e.printStackTrace();
      }
		}
	}

	/**
	 * this thread manages the connectios of the server thread
	 * @author Stephan Richter
	 *
	 */
	private class ManagerThread extends Thread {
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(Ports.registrationPort());
				idleHandles=new TreeSet<ClientHandle>(ObjectComparator.get());
				workingClients=new TreeSet<ClientHandle>(ObjectComparator.get());
				while (true) {
					System.out.print("Waiting for client...");
					try {
						(new ClientThread(serverSocket.accept())).start();
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creates a new server instance for server-client interactions
	 * @throws IOException
	 */
	public Master() throws IOException {
		(new ManagerThread()).start();
	}

	/**
	 * gets one of the client handles marked as idle
	 * @return a client handle
	 */
	public synchronized ClientHandle getIdleHandle() {
		if (idleHandles==null || idleHandles.isEmpty()) return null;
		ClientHandle result=idleHandles.first();
		idleHandles.remove(result);
		workingClients.add(result);
		return result;
	}	


	/**
	 * broadcasts a message object to all idle clients
	 * @param object teh object that shall be sent to all idle clients
	 * @throws IOException
	 */
	protected synchronized void broadCastToIdle(Serializable object) throws IOException {
		for (Iterator<ClientHandle> it = idleHandles.iterator();it.hasNext();) it.next().send(object);
  }
	
	/**
	 * get a list of all connected clients
	 * @return the list of all clients, which are currently connected to this master object
	 */
	private synchronized TreeSet<ClientHandle> allClients(){
		TreeSet<ClientHandle> result=new TreeSet<ClientHandle>(ObjectComparator.get());
		result.addAll(idleHandles);
		result.addAll(workingClients);
		return result;
	}
	
	/**
	 * broadcast a message to all connected clients (idle or not)
	 * @param object the object, that shall be sent
	 * @throws IOException
	 */
	protected synchronized void broadCastToAll(Serializable object) throws IOException{
		for (Iterator<ClientHandle> it = allClients().iterator();it.hasNext();) it.next().send(object);
	}
	
	/**
	 * broadcasts a disconnect signal to clients
	 * @param onlyIdle if set to true, the disconnect signal will be sent only to idle clients, if set to false, all clients will recieve it
	 * @throws IOException
	 */
	public synchronized void disconnect(boolean onlyIdle) throws IOException {
		Signal s=new Signal(Signal.KILL);
		TreeSet<ClientHandle> handles = (onlyIdle)?idleHandles:allClients();
		while(!handles.isEmpty()) {
			ClientHandle handle = handles.first();
			handle.send(s);
			idleHandles.remove(handle);
			workingClients.remove(handle);
		}
  }
	
	/**
	 * checks, whether we are out of idle clients
	 * @return true, only if there are no more idle clients
	 */
	private synchronized boolean noIdleHandlesExist(){
		return idleHandles==null || idleHandles.isEmpty();
	}

	/**
	 * waits, until at least one client is marked as idle
	 */
	private void waitForIdleHandles() {
		while (noIdleHandlesExist()) try {
	      Thread.sleep(10);
      } catch (InterruptedException e) {}
  }	
	
	/**
	 * test method
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Master m = new Master();
		System.out.println("waiting for idle handles...");
		m.waitForIdleHandles();
		System.out.println("starting to broadcast...");
		for (int i=0; i<10; i++){
			m.broadCastToIdle("Test "+i);			
			try {
	      Thread.sleep(2000);
      } catch (InterruptedException e) {}
		}
		m.broadCastToIdle(new Signal(Signal.KILL));
		System.exit(0);
	}
}