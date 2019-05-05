/* CHAT ROOM ChatServer.java
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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.io.*;

public class ChatServer extends Observable {
	private List<Socket> clients;
	
	private int port;
	
	public ChatServer() {
		clients = new ArrayList<Socket>();	
		port = 4242;
	}
	
	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(port);
		while (true) {
			Socket clientSocket = serverSock.accept();
			
			clients.add(clientSocket);
			
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			
			Thread t = new Thread(new ClientHandler(clientSocket));
			t.start();
			
			this.addObserver(writer);
			System.out.println("got a connection");
		}
	}
	class ClientHandler implements Runnable {
		private BufferedReader reader;

		public ClientHandler(Socket clientSocket) {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("server read "+message);
					setChanged();
					notifyObservers(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
