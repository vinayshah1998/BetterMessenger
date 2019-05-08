/* CHAT ROOM ChatClient.java
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

import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.text.ParsePosition;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;


public class ChatClient extends Application {
	private JTextArea incoming;
	private JTextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		initView(primaryStage);
	}
	
	private void initView(Stage primaryStage) {
		primaryStage.setTitle("Headscroll");
		
		HBox mainGrid = new HBox();
		VBox messageSpace = new VBox();
		
		// Message Space
		TextField typeMessages = new TextField();
		typeMessages.setEditable(true);
		typeMessages.setPrefWidth(200);
	
		TextField viewMessages = new TextField();
		viewMessages.setEditable(false);
		viewMessages.setPrefSize(300, 200);
		
		// Add children
		messageSpace.getChildren().addAll(viewMessages, typeMessages);
		
		primaryStage.setScene(new Scene(messageSpace, 280, 220));
		primaryStage.show();		
	}
//
//	private void setUpNetworking() throws Exception {
//		Socket sock = new Socket("127.0.0.1", 4242);
//		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
//		reader = new BufferedReader(streamReader);
//		writer = new PrintWriter(sock.getOutputStream());
//		System.out.println("networking established");
//		Thread readerThread = new Thread(new IncomingReader());
//		readerThread.start();
//	}

//	class IncomingReader implements Runnable {
//
//		@Override
//		public void run() {
//
//			String message;
//
//			try {
//				while ((message = reader.readLine()) != null) {
//
//					// message received from the server
//					String[] splitMessage = message.trim().split("\\s+");
//
//					// instruction from server is to update
//					if (splitMessage[0].equals("GETONLINE")) {
////						Platform.runLater(new Runnable() {
////							@Override
////							public void run() {
////								loggedIn(splitMessage[2].split(nameSeparator));
////							}
////						});
//					}
//
//					// server indicates another user wants to either start messaging or send a message to an existing chat
//					else if (isNumeric(splitMessage[0])) {
//						Platform.runLater(() -> {
//
//							int ID = Integer.parseInt(splitMessage[0]);
//
//							// chat window ID is already contained within this user's list of open chat windows
//							if (chatWindows.containsKey(ID)) {
//								chatWindows.get(ID).updateChat(splitMessage);
//							}
//
//							// new chat being initiated
//							else {
//								startChat(splitMessage);
//							}
//						});
//					}
//
//					// server telling client username is taken, already exists
//					else if (splitMessage[0].equals("USEREXISTS")) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								userExists(splitMessage);
//							}
//						});
//					}
//
//					// server telling client user is already logged in
//					else if(splitMessage[0].equals("ALREADYLOGGEDIN")) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								alreadyLogged(splitMessage);
//							}
//						});
//					}
//
//					// server telling client the entered password is incorrect
//					else if (splitMessage[0].equals("WRONGPASS")) {
//						Platform.runLater(new Runnable() {
//							@Override
//							public void run() {
//								wrongPass(splitMessage);
//							}
//						});
//					}
//				}
//			} catch (IOException ex) {
//				if(ex instanceof SocketException) {}
//				else ex.printStackTrace(); 	}
//		}
//	}

	public static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
