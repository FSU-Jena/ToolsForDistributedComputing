package edu.fsuj.csb.distributedcomputing.tools;
import java.io.Serializable;


/**
 * implements special objects to control server-client connections
 * @author Stephan Richter
 *
 */
public class Signal implements Serializable{
	private static final long serialVersionUID = 02;
	public final static int KILL=0;
	public final static int IDLE=1;
	public final static int DONE=2;
	private int type;
	
	/**
	 * create a new signal
	 * @param type
	 */
	public Signal(int type) {
		this.type=type;
  }
	
	/**
	 * return the type of this signal
	 * @return the type
	 */
	public int type(){
		return type;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		switch (type){
		case KILL: return "Signal(KILL)";
		case IDLE: return "Signal(IDLE)";
		case DONE: return "Signal(DONE)";
		}
		return super.toString();
	}
}
