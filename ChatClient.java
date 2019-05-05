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
import javafx.application.Application;
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


public class ChatClient extends Application {
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
	
//	private void setUpNetworking() throws Exception {
//
//	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
