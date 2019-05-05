/* CHAT ROOM ClientObserver.java
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Vinay Shah
 * vss452
 * 16205
 * Vignesh Ravi
 * vgr325
 * 16225
 * Slip days used: <0>
 * Spring 2019
 */

package assignment7;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends PrintWriter implements Observer {
	public ClientObserver(OutputStream out) {
		super(out);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		this.print(arg);
		this.flush();
	}
	
}
