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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {

	public static final String user_conv = "user_conv%";
	public static final String user_conv_add = "user_conv_add%";
	public static final String user_text = "user_text%";
	public static final String sign_in = "sign_in%";
	public static final String user = "user%";

	private BufferedReader reader;
	private PrintWriter writer;
	private String serverAddress;
	private String userName;
	private String id;
	boolean loggedIn = false;
	private String conversationID;
	boolean startedConversation = false;

	HashMap<String, String> onlineUsers; // id --> username
    HashMap<String, Conversation> conversations;
	ObservableList<String> messages;
	ObservableList<Pair> users;
	ObservableList<Pair> convoNames;

	@FXML
	private ListView<String> chatList;

	@FXML
	private javafx.scene.control.Button sendButton;

    @FXML
    private javafx.scene.control.Button newChatButton;

    @FXML
    private javafx.scene.control.Button addToChatButton;

	@FXML
	private TextArea outgoingMessage;

	@FXML
	public ListView<Pair> onlineUsersList;

	@FXML
	private ListView<Pair> conversationList;

	@FXML
	private TextField searchField;

	@FXML
	private Button searchButton;

	public ChatClient() {
	    startUpDialog();

		try {
			setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//chatList = new ListView<String>();
		//onlineUsersList = new ListView<>();
		messages = FXCollections.observableArrayList();
		users = FXCollections.observableArrayList();
		convoNames = FXCollections.observableArrayList();
		conversations = new HashMap<>();
		onlineUsers = new HashMap<>();
		conversationID = null;
	}

	public void login() {
		// send client to server
		writer.println(sign_in + userName);
		writer.flush();

		//Disable sending empty messages
		sendButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		outgoingMessage.textProperty().addListener((observable, oldValue, newValue) -> {
			sendButton.setDisable(newValue.trim().isEmpty());
		});

        //setup cell factories for listviews

        conversationList.setCellFactory(param -> new ListCell<Pair>() {
            @Override
            protected void updateItem(Pair item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getKey() == null) {
                    setText(null);
                } else {
                    setText((String)item.getValue());
                    setWrapText(true);
                }
            }
        });

        onlineUsersList.setCellFactory(param -> new ListCell<Pair>() {
            @Override
            protected void updateItem(Pair item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getKey() == null) {
                    setText(null);
                } else {
                    setText((String)item.getValue());
                }
            }
        });
		searchField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
								String oldValue, String newValue) {

				String searchTerm = searchField.getText();

				if (searchTerm.equals("")) { // add back all conversations
					convoNames.clear();
					for (String convoID: conversations.keySet()) {
						// build name
						String convoName = "";
						for (String memberID: conversations.get(convoID).getMembers()) {
							if (!memberID.equals(id)) {
								if (convoName.equals("")) {
									convoName += onlineUsers.get(memberID);
								} else {
									convoName += ", " + onlineUsers.get(memberID);
								}
							}
						}
						convoNames.add(new Pair(convoID, convoName));
					}
					conversationList.setItems(convoNames);
				} else { // there's some search term in the field
					// remove conversations that don't match
					ArrayList<Pair> needToRemove = new ArrayList<>();
					for (Pair conv: convoNames) {
						if (!conv.getValue().toString().contains(searchTerm)) {
							needToRemove.add(conv);
						}
					}
					convoNames.removeAll(needToRemove);
					conversationList.setItems(convoNames);
					// add conversations that do
					ArrayList<Pair> needToAdd = new ArrayList<>();
					for (String convoID: conversations.keySet()) {
						boolean addToList = false;
						for (String member: conversations.get(convoID).getMembers()) {
							if (!member.equals(id)) {
								if (onlineUsers.get(member).contains(searchTerm)) {
									addToList = true;
									break;
								}
							}
						}
						if (addToList) {
							// figure out if convo already exists in list
							boolean alreadyExists = false;
							for (Pair s: convoNames) {
								if (s.getKey().equals(convoID)) {
									alreadyExists = true;
								}
							}
							if (!alreadyExists) {
								// build name
								String convoName = "";
								for (String memberID: conversations.get(convoID).getMembers()) {
									if (!memberID.equals(id)) {
										if (convoName.equals("")) {
											convoName += onlineUsers.get(memberID);
										} else {
											convoName += ", " + onlineUsers.get(memberID);
										}
									}
								}
								needToAdd.add(new Pair(convoID, convoName));
							}
						}
					}
					convoNames.addAll(needToAdd);
					conversationList.setItems(convoNames);
				}
			}
		});
	}

	@FXML
	void sendMessage(javafx.event.ActionEvent event) {

		// send message to server and have new lines become spaces
		writer.println(user_text + conversationID  + "%" + userName + "%" +  outgoingMessage.getText().replaceAll("\n", " "));
		writer.flush();

		outgoingMessage.setText("");
		outgoingMessage.requestFocus();
	}

	@FXML
	void startConversation(ActionEvent event) {
        ArrayList<String> conversationMembers = new ArrayList<>();
        String userIds = "";
		conversationMembers.add(id); // current user

        // get desired members of conversation
        for (int i = 0; i < onlineUsersList.getSelectionModel().getSelectedItems().size(); i ++) {
        	if (onlineUsersList.getSelectionModel().getSelectedItems().get(i) != null) {
				userIds += ("%" +  (onlineUsersList.getSelectionModel().getSelectedItems().get(i).getKey()));
				conversationMembers.add((String) onlineUsersList.getSelectionModel().getSelectedItems().get(i).getKey()); // all other users
			}
		}

        startedConversation = true;

        // check if conversation already exists
		for (Conversation conversation: conversations.values()) {
			if ((conversation.getMembers().containsAll(conversationMembers)) && (conversation.getMembers().size() == conversationMembers.size())) {
				// it exists, pull up old conversation
				conversationID = conversation.getId();
				messages.clear();
				messages.addAll(conversations.get(conversationID).getMessages());
				chatList.setItems(messages);
				return;
			}
		}

        if(userIds != "") {
            writer.println(user_conv + id + userIds);
            writer.flush();
        }
	}

	@FXML
	void resumeConversation(MouseEvent event) {
		if(conversationList.getSelectionModel().getSelectedItem() == null) {
			return;
		}
        String conversationId = (String) conversationList.getSelectionModel().getSelectedItem().getKey();
        chatList.setItems(messages);
        messages.clear();
        messages.addAll(conversations.get(conversationId).getMessages());
        chatList.setItems(messages);
        conversationID = conversationId;
    }

    @FXML
    void addToChat(ActionEvent event) {
		ArrayList<String> conversationMembers = new ArrayList<>();
		String userIds = "";
		conversationMembers.addAll(conversations.get(conversationID).getMembers());

		// collect desired users to add and IGNORE duplicate users that are already in conversation
		  for (int i = 0; i < onlineUsersList.getSelectionModel().getSelectedItems().size(); i ++) {
			if (onlineUsersList.getSelectionModel().getSelectedItems().get(i) != null) {
				if (!conversations.get(conversationID).getMembers().contains(onlineUsersList.getSelectionModel().getSelectedItems().get(i).getKey())) {
					userIds += ("%" +  (onlineUsersList.getSelectionModel().getSelectedItems().get(i).getKey()));
					conversationMembers.add((String) onlineUsersList.getSelectionModel().getSelectedItems().get(i).getKey()); // all other users
				}
			}
		}

		// check if conversation already exists
		for (Conversation conversation: conversations.values()) {
			if ((conversation.getMembers().containsAll(conversationMembers)) && (conversation.getMembers().size() == conversationMembers.size())) {
				// it exists, pull up old conversation
				conversationID = conversation.getId();
				messages.clear();
				messages.addAll(conversations.get(conversationID).getMessages());
				chatList.setItems(messages);
				return;
			}
		}
		if (userIds != "") {
			writer.println(user_conv_add + conversationID + userIds);
			writer.flush();
		}
    }

	public void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket(serverAddress, 3242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	class IncomingReader implements Runnable {
		public void run() {
			String message;
			String messageType;


			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("client read " + message);

					String[] messageParts = message.split("%");
					messageType = messageParts[0];

					switch (messageType) {
						case "user":
							System.out.println(message);

							//adds entire list of users to listview
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
                                    users.clear();
                                    for (int i = 1; i < messageParts.length; i = i + 2) {
                                        if(!messageParts[i].equals(id)) {
                                            Pair userPair = new Pair(messageParts[i], messageParts[i + 1]);
                                            users.add(userPair);
                                            onlineUsers.put(messageParts[i], messageParts[i + 1]);
                                        }
                                    }
									onlineUsersList.setItems(users);
								}
							});
							break;

						case "MESSAGE":
							boolean belongToConversation1 = false;

							if (conversations.containsKey(messageParts[1])) {
								belongToConversation1 = true;
							}

							if (belongToConversation1) {
								conversations.get(messageParts[1]).addMessage(messageParts[2] + "\n\t" + messageParts[3]);
								if (messageParts[1].equals(conversationID)) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											messages.add(messageParts[2] + "\n\t" + messageParts[3]);
											chatList.setItems(messages);
											chatList.scrollTo(chatList.getItems().size() - 1);
										}
									});
								}
							}
							break;

						case "user_conv":

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									boolean belongToConversation = false;
									for (int i = 2; i < messageParts.length; i++) {
										if (messageParts[i].equals(id)) { // only if you're being added to this new conversation
											belongToConversation = true;
											break;
										}
									}
									if (belongToConversation) {
										String conversationName = "";
										ArrayList<String> members = new ArrayList<>();
										for (int i = 2; i < messageParts.length; i++) {
											members.add(messageParts[i]);
											if (!messageParts[i].equals(id)) {
												if (conversationName.equals("")) {
													conversationName += onlineUsers.get(messageParts[i]);
												} else {
													conversationName += ", " + onlineUsers.get(messageParts[i]);
												}
											}
										}
										outgoingMessage.clear();
										outgoingMessage.setPromptText("Write a message to " + conversationName);
										convoNames.add(new Pair(messageParts[1], conversationName));
										conversationList.setItems(convoNames);
										conversations.put(messageParts[1], new Conversation(messageParts[1], members));
										if (conversationID == null) { // first conversation ever, so update chatList and display
											conversationID = messageParts[1];
										}

										if (startedConversation) { // need to refresh feed
											conversationID = messageParts[1];
											messages.clear();
											messages.addAll(conversations.get(conversationID).getMessages());
											chatList.setItems(messages);
										}
									}
								}
							});
							break;

						//set client ID as received from server, only if not yet logged in
						case "sign_in":
							if (!loggedIn) {
								id = new String(messageParts[1]);
								loggedIn = true;
							}
							break;

						case "REMUSER" :
						    String userID = messageParts[1];
						    //look through users and remove the correct one
							for (Pair userPair: users) {
							    if(userPair.getKey().equals(userID)) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            users.remove(userPair);
                                            onlineUsersList.setItems(users);
                                        }
                                    });
                                }
                            }

                            onlineUsers.remove(userID);

                            for (Conversation conversation: conversations.values()) {
                                conversation.removeMember(userID);
                            }
                            break;

                        case "user_conv_add" :
                            //newConversationMember identifies all of the members being added to the convo
                            boolean newConversationMember = false;
                            boolean conversationMember = false;

                            //this lets the client know if they are affected by the changes
                            for (int i = 2; i < messageParts.length; i++) {
                                if (messageParts[i].equals(id)) { // only if you're actually in this conversation
                                    conversationMember = true;
                                    break;
                                }
                            }

                            //this lets the client know if they have to create a new conversation because they're being added to it
							if(conversations.get(messageParts[1]) == null) {
                            	newConversationMember = true;
							}

							//client creates new conversation
							if (newConversationMember) {
								String conversationName = "";
								ArrayList<String> members = new ArrayList<>();
								for (int i = 2; i < messageParts.length; i++) {
									members.add(messageParts[i]);
									if (!messageParts[i].equals(id)) {
										if (conversationName.equals("")) {
											conversationName += onlineUsers.get(messageParts[i]);
										} else {
											conversationName += ", " + onlineUsers.get(messageParts[i]);
										}
									}
								}

								String finalConversationName = conversationName;
								Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        convoNames.add(new Pair(messageParts[1], finalConversationName));
                                        conversationList.setItems(convoNames);
                                        conversations.put(messageParts[1], new Conversation(messageParts[1], members));
                                    }
                                });
                            }


                            //I am already in the conversation being changed
                            else if(conversationMember && !newConversationMember) {
								String conversationName = "";
								ArrayList<String> members = new ArrayList<>();
								for (int i = 2; i < messageParts.length; i++) {
									members.add(messageParts[i]);
									//only add other people's usernames to the chat name
									if (!messageParts[i].equals(id)) {
										if (conversationName.equals("")) {
											conversationName += onlineUsers.get(messageParts[i]);
										} else {
											conversationName += ", " + onlineUsers.get(messageParts[i]);
										}
									}
								}

								String finalConversationName = conversationName;
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										for (Pair pair: convoNames) {
											//finds and replaces conversation pair
											if (pair.getKey().equals(messageParts[1])) {
												convoNames.set(convoNames.indexOf(pair), new Pair(messageParts[1], finalConversationName));
												break;
											}
										}
										conversationList.setItems(convoNames);

										conversations.get(messageParts[1]).clearMembers();
										for (String member : members) {
											conversations.get(messageParts[1]).addMember(member);
										}

									}
								});
							}
							break;
					}
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private Conversation customMethod(String[] messageParts, HashMap<String, String> onlineUsers ){
		String conversationName = "";
		ArrayList<String> members = new ArrayList<>();
		for (int i = 2; i < messageParts.length; i++) {
			members.add(messageParts[i]);
			//only add other people's usernames to the chat name
			if (!messageParts[i].equals(id)) {
				if (conversationName.equals("")) {
					conversationName += onlineUsers.get(messageParts[i]);
				} else {
					conversationName += ", " + onlineUsers.get(messageParts[i]);
				}
			}
		}

		return new Conversation(conversationName,members);
	}

	private void startUpDialog() {
		//show dialog to prompt for login info and server IP address

		//thanks to this dude: http://code.makery.ch/blog/javafx-dialogs-official/
		// Create the custom dialog.
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Login Dialog");
		dialog.setHeaderText("Please enter a username and server IP address:");

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		TextField serverInput = new TextField();
		serverInput.setPromptText("127.0.0.1");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Server Address:"), 0, 1);
		grid.add(serverInput, 1, 1);

		// Enable/Disable login button depending on whether a username was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

		loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});

		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
				System.exit(0);
			}
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> username.requestFocus());

		dialog.showAndWait();

		dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		serverAddress = new String(serverInput.getText());
		userName = new String(username.getText());
		System.out.println(serverAddress);
	}

}