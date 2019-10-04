/* CHAT ROOM <MyClass.java>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ChatServer extends Observable{

	public static final String CONV = "user_conv";
	public static final String TEXT = "user_text";
	public static final String LOGIN = "sign_in";

	HashMap<String, Conversation> conversationDirectory;	 //ID to Conversation
	HashMap<String, String> onlineUsers;	//ID to userName
	Set<String> users;

	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		onlineUsers = new HashMap<>();
		conversationDirectory = new HashMap<>();

		ServerSocket serverSock = new ServerSocket(3242);
		while (true) {
			Socket clientSocket = serverSock.accept();
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
			String userID = null;
			String message;
			String messageType;

			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("server read " + message);

					String[] messageParts = message.split("%");
					messageType = messageParts[0];

					String allUsersMessage = new String("user");

					if (messageType.equals(LOGIN)) {
						//System.out.println(Arrays.toString(messageParts));
						userID = UUID.randomUUID().toString();
						onlineUsers.put(userID, messageParts[1]);

						setChanged();
						notifyObservers(LOGIN  + "%" + userID);

						users = onlineUsers.keySet();
						for (String id: users) {
							allUsersMessage = allUsersMessage + "%" + id + "%" + onlineUsers.get(id);
						}
						setChanged();
						notifyObservers(allUsersMessage);
					} else if (messageType.equals(TEXT)) {
						setChanged();
						notifyObservers("MESSAGE%"+ messageParts[1] + "%" + messageParts[2] + "%" + messageParts[3]);
					} else if (messageType.equals(CONV)) {
						Conversation conversation = new Conversation();

						String members = "";
						for(int i = 1; i < messageParts.length; i++) {
							conversation.addMember(messageParts[i]);
							members += "%" + messageParts[i];
						}
						conversation.setId(UUID.randomUUID().toString());
						conversationDirectory.put(conversation.getId(), conversation);
						setChanged();

						notifyObservers("user_conv%" + conversation.getId() + members);

					} else if (messageType.equals("user_conv_add")) {
						String members2 = "";

						if(conversationDirectory.get(messageParts[1]) != null) {
							for (String member : conversationDirectory.get(messageParts[1]).getMembers()) {
								members2 += "%" + member ;
							}

							for(int i = 2; i < messageParts.length; i++) {
								conversationDirectory.get(messageParts[1]).addMember(messageParts[i]);
								members2 += "%" + messageParts[i] ;
							}

							setChanged();
							notifyObservers("user_conv_add%" + messageParts[1] + members2);
						}

					}
				}
			} catch (IOException e) {
				//remove from online users when connection resets
				if(e instanceof SocketException) {
					onlineUsers.remove(userID);
					setChanged();
					notifyObservers("REMUSER%" + userID);
				}
				else {
					e.printStackTrace();
				}
			}
		}
	}
}
