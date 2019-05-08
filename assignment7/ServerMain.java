/* CHAT ROOM ServerMain.java
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
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;
import java.io.*;

public class ServerMain extends Observable {
//	private List<String> clients;
	private Map<String, ClientObserver> observedUsers;
	private List<ChatRoom> rooms;
	private boolean changeInUsers;

	private ServerSocket ssocket;
	
	private int port;
	
	public ServerMain() {
		rooms = new ArrayList<ChatRoom>();
		observedUsers = new HashMap<String, ClientObserver>();
		port = 4242;
	}
	
	public static void main(String[] args) {
		try {
			new ServerMain().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		ssocket = new ServerSocket(port);
		while (true) {
			Socket clientSocket = ssocket.accept();
			System.out.println("Client connected " + clientSocket);
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket, writer));
			t.start();
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private ClientObserver writer;
		public String username;

		public ClientHandler(Socket clientSocket, ClientObserver writer) {
			Socket sock = clientSocket;
			try {
				this.reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				this.writer = writer;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

//		public void run() {
//			String message;
//			try {
//				while ((message = reader.readLine()) != null) {
//					System.out.println("server read "+message);
//					setChanged();
//					notifyObservers(message);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}


		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					//separate message into commands
					String[] splitMessage = message.trim().split("\\s+");

					//sending to specific chat room?
					if (isNumeric(splitMessage[0])){
						rooms.get(Integer.parseInt(splitMessage[0])).sendMessage(message);
					}

					//create a chat room when user requests to chat with others
					//determine whether to create a new room or use an existing one
					else if (splitMessage[0].equalsIgnoreCase("create_room")){
						//want to see if a room exists with all people selected, they should be the other arguments of splitMessage

						//grab the list of people in the group
						String[] group = Arrays.copyOfRange(splitMessage, 1, splitMessage.length - 1);
						ArrayList<String> groupAL = new ArrayList<>(Arrays.asList(group));
						Integer groupID = null;
						for (ChatRoom room : rooms){
							if ((room.users.containsAll(groupAL)) && (room.users.size() == groupAL.size())){
								groupID = room.ID;
								break;
							}
						}

						if (groupID == null){
							ChatRoom newRoom = new ChatRoom();
							rooms.add(newRoom);
							newRoom.ID = rooms.indexOf(newRoom);
							newRoom.addUsers(groupAL);
							newRoom.sendMessage("" + Integer.toString(newRoom.ID) + " " + "server" + " " + "This is a new chat between: " + groupAL);
						}
					}

					//adding a user to an existing room
					else if (splitMessage[0].equalsIgnoreCase("join_room")){
						ArrayList<String> userAL = new ArrayList<String>(Arrays.asList(splitMessage[1]));
						ChatRoom existingRoom = rooms.get(Integer.parseInt(splitMessage[2]));
						existingRoom.addUsers(userAL);
						existingRoom.sendMessage("server: " + userAL.get(0) + " has joined the room.");
					}

					// a user should send in its user name when it is first created
					//determine whether to create a new user, or if one exists then return a message saying so
					else if (splitMessage[0].equalsIgnoreCase("new_user")){
						if (observedUsers.containsKey(splitMessage[1])){
//							writer.println("server: This username already exists, please try another username.");
						} else {
							this.username = splitMessage[1];
							observedUsers.put(username, writer);
							changeInUsers = true;
						}
					}

					// retrieve all online people if there has been a change in
					// # of clients and print out change to every client
					else if (splitMessage[0].equalsIgnoreCase("show_online")){
						if(changeInUsers) {
							changeInUsers = false;
							Set<String> keys = 	observedUsers.keySet();
							String names = "";

							// create String with all online user names
							for(String user: keys) {
								names += user + " ";
							}


							ChatRoom temp = new ChatRoom();
							ArrayList<String> splitNames = new ArrayList<>(Arrays.asList(names.trim().split("\\s+")));
							temp.addUsers(splitNames);
							// return back GETONLINE string with user names separated by nameSeparator
							names = ("show_online" + " " + splitMessage[1] + " " + names);

							// use chat room observer pattern to send message
							temp.sendMessage(names);
						}
					}


					// LOGOUT removes user from our HashMap
					else if (splitMessage[0].equalsIgnoreCase("logout")){
						observedUsers.remove(username);
					}
				}

			} catch (IOException e) {

				e.printStackTrace();

			}
		}

	}

	public static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}


	class ChatRoom extends Observable {

		public List<String> users = new ArrayList<String>();
		public int ID;

		public void addUsers(ArrayList<String> newUsers) {
			for (int i = 0; i < newUsers.size(); i++){
				if(observedUsers.containsKey(newUsers.get(i))) {
					addObserver(observedUsers.get(newUsers.get(i)));
					users.add(newUsers.get(i));
				}
			}
		}

		public void sendMessage(String message) {
			setChanged();
			notifyObservers(message);
		}
	}


	
}
