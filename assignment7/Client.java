package assignment7;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client extends Application {

    private String name;
    private Socket sock;
    private BufferedReader reader;
    private PrintWriter writer;
    private static final String separator = " ";
    private static final String nameSeparator = Character.toString((char) 29);
    private Stage home = null;
    private HashMap<Integer, ChatWindow> chatWindows = new HashMap<Integer, ChatWindow>();
    private VBox open;
    private Color mine = Color.BLACK;
    private Color theirs = Color.BLACK;
    private String font = "Juice ITC";
    private String buttonColor = "#b7cedd";

    /**
     * This function takes care of the setup of the GUI when a chat client is made.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        home = primaryStage;
        open = new VBox();
        open.setPadding(new Insets(10, 40, 40, 40));
        open.setSpacing(10);
        primaryStage.setTitle("Chat Room");

        Label title = new Label("Welcome to Chat Room!");
        title.setTextFill(Color.DARKCYAN);
        title.setStyle("-fx-font-weight: bold");
        title.setFont(Font.font(font, 30));

        Label ipInstruction = new Label("Enter IP Address:");
        TextField ip = new TextField();
        ip.setPromptText("IP Address");

        Label error = new Label();

        // user inputs IP address of server trying to connect to
        Button enterIP = new Button("Connect");
        enterIP.setStyle("-fx-base: " + buttonColor + ";");
        enterIP.setOnAction(new EventHandler<ActionEvent>() {

            // after IP address entered, set up network and then prompt log in screen
            @Override
            public void handle(ActionEvent arg0) {
                if(!ip.getText().equals("")) {
                    try {
                        setUpNetworking(ip.getText());
                        loginScreen();
                    } catch (UnknownHostException e) {
                        error.setText("Invalid IP, reenter.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ip.setOnKeyPressed(new EventHandler<KeyEvent>() {

            // enter key
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER){
                    if(!ip.getText().equals("")) {
                        try {
                            setUpNetworking(ip.getText());
                            loginScreen();
                        } catch (UnknownHostException e) {
                            error.setText("Invalid IP, reenter.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        open.getChildren().addAll(title, ipInstruction, ip, enterIP, error);
        Scene scene = new Scene(open, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * After a valid IP address is entered and a network is established, this function is called to
     * bring up the log in screen for the user
     */
    public void loginScreen() {

        open.getChildren().clear();

        open = new VBox();
        open.setPadding(new Insets(10, 40, 40, 40));
        open.setSpacing(10);

        Label title = new Label("Welcome to Chat Room!");
        title.setTextFill(Color.DARKCYAN);
        title.setStyle("-fx-font-weight: bold");
        title.setFont(Font.font(font, 30));

        Label instruction = new Label("Please enter a username:");
        TextField username = new TextField();
        username.setPromptText("Username");

//        Label instruction2 = new Label("Please enter a password:");
//        TextField password = new TextField();
//        password.setPromptText("Password");

//        Button createAcc = new Button("Create Account");
//        createAcc.setStyle("-fx-base: " + buttonColor + ";");
//        createAcc.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent arg0) {
//                String user = username.getText();
////                String pwd = password.getText();
//
//                if (!user.equals("")) {
//                    name = user;
//
//                    // send the new user info to the server
//                    writer.println("new_user" + " " + user + " ");
//                    writer.flush();
//
//                    // get a list of current users who are online
//                    writer.println("show_online" + separator + name);
//                    writer.flush();
//                }
//            }
//
//        });

        Button login = new Button("Login");
        login.setStyle("-fx-base: " + buttonColor + ";");
        login.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                String user = username.getText();

                if (!user.equals("")) {
                    name = user;

                    // send the new user info to the server
                    writer.println("new_user" + " " + user + " ");
                    writer.flush();

                    // get a list of current users who are online
                    writer.println("show_online" + " " + name);
                    writer.flush();
                }
            }

        });

        open.getChildren().addAll(title, instruction,  username , login);
        Scene scene = new Scene(open, 300, 300);
        home.setScene(scene);
        home.show();
    }

    /**
     * This function sets up the networking to the specified IP address and port and starts the thread.
     * @param IP is a String that holds the IP address entered by the user
     * @throws Exception
     */
    private void setUpNetworking(String IP) throws Exception {

        this.sock = new Socket(IP, 4242);
        InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
        reader = new BufferedReader(streamReader);

        try
        {
            writer = new PrintWriter(sock.getOutputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    /**
     * This function is called when the user selects "Make Account" in the login screen. This function will
     * determines whether the login username entered by a user already exists in the server's database.
     * @param message is unused
     */
    public void userExists(String[] message) {


        for(ListIterator<Node> iterator = open.getChildren().listIterator(); iterator.hasNext();) {
            Node currentNode = iterator.next();
            if (currentNode instanceof Label && ((Label)currentNode).getText().contains("ERROR")) {
                iterator.remove();
            }
        }

        String error = "ERROR: Username already exists. Enter another username.";
        Label notif = new Label();
        notif.setText(error);
        notif.setWrapText(true);
        open.getChildren().addAll(notif);


    }

    /**
     * This function is called when the user selects "Login" at the login screen. The function will determine whether or not the
     * user is already in the list of userObservers logged in at the moment.
     * @param message is not used
     */
    public void alreadyLogged(String[] message) {

        for(ListIterator<Node> iterator = open.getChildren().listIterator(); iterator.hasNext();) {
            Node currentNode = iterator.next();
            if (currentNode instanceof Label && ((Label)currentNode).getText().contains("ERROR")) {
                iterator.remove();
            }
        }

        String error = "ERROR: User already logged in. Enter another username.";
        Label notif = new Label();
        notif.setText(error);
        notif.setWrapText(true);
        open.getChildren().addAll(notif);

    }

    /**
     * After user selects the "login" button in the login screen, this function checks to see if the username given matches
     * the password saved in the text file database.
     * @param message
     */
    public void wrongPass(String[] message) {

        for(ListIterator<Node> iterator = open.getChildren().listIterator(); iterator.hasNext();) {
            Node currentNode = iterator.next();
            if (currentNode instanceof Label && ((Label)currentNode).getText().contains("ERROR")) {
                iterator.remove();
            }
        }

        String error = "ERROR: Invalid Password. Please Retry.";
        Label notif = new Label();
        notif.setText(error);
        notif.setWrapText(true);
        open.getChildren().addAll(notif);


    }

    /**
     * After user successfully logs in, this function sets up the new user interface for the "home screen". "Home screen" includes
     * options to chat with other users, customize colors, etc.
     * @param available
     */
    public void loggedIn(String[] available) {

        //close old stage
        double x = home.getX();
        double y = home.getY();
        home.close();

        home = new Stage();
        home.setTitle("The Chat Room");
        home.setX(x);
        home.setY(y);

        Label space1 = new Label("");
        Label space2 = new Label("");
        Label space3 = new Label("");

        VBox chat = new VBox();
        chat.setPadding(new Insets(10, 40, 40, 40));
        chat.setSpacing(5);

        Label welcome = new Label("Welcome, " + name + "!");
        welcome.setTextFill(Color.DARKCYAN);
        welcome.setStyle("-fx-font-weight: bold");
        welcome.setFont(Font.font(font, 40));

        Label people = new Label("Who would you like to chat with?");

        chat.getChildren().addAll(welcome, space1, people);

        // CheckBox of all users currently online (available is the array of online users)
        CheckBox[] online = new CheckBox[available.length-1];
        int j = 0;
        for (int i = 0; i < available.length; i++) {
            if(!(available[i].equals(name))) {
                online[j] = new CheckBox(available[i]);
                chat.getChildren().add(online[j]);
                j++;
            }
        }

        // start chat with other online user(s)
        Button done = new Button("Start Chat");
        done.setStyle("-fx-base: " + buttonColor + ";");
        done.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {

                String names = "";
                for(int i = 0; i < online.length; i++) {
                    if (online[i].isSelected())
                        names += online[i].getText() + nameSeparator;
                }

                if (names.equals("")) {
                    return;
                }

                String message = "NEWCHAT" + separator + name + separator + names;
                writer.println(message);
                writer.flush();

            }});

        Label myColor = new Label("Select your color:");

        // give users the option of customizing chat colors
        ColorPicker colorPickerMine = new ColorPicker(Color.BLACK);
        colorPickerMine.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                mine = colorPickerMine.getValue();
            }
        });

        Label theirColor = new Label("Select their color:");

        ColorPicker colorPickerTheirs = new ColorPicker(Color.BLACK);
        colorPickerTheirs.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                theirs = colorPickerTheirs.getValue();
            }
        });

        // log out of the system
        Button logOut = new Button("Log Out");
        logOut.setStyle("-fx-base: " + buttonColor + ";");
        logOut.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {


                for (ChatWindow chats : chatWindows.values()) {
                    chats.chat.close();
                }

                writer.println("LOGOUT" + separator + name);
                writer.flush();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                writer.println("GETONLINE" + separator + name);
                writer.flush();

                try {
                    sock.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                home.close();
                exit();
            }
        });

        chat.getChildren().addAll(done, space2, myColor, colorPickerMine, theirColor, colorPickerTheirs, space3, logOut);
        Scene realScene = new Scene(chat, 300, 400);
        home.setScene(realScene);

        // set up so that if the user closes the home window, it effectively logs them out of the system
        home.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                for (ChatWindow chats : chatWindows.values()) {
                    chats.chat.close();
                }

                writer.println("LOGOUT" + separator + name);
                writer.flush();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                writer.println("GETONLINE" + separator + name);
                writer.flush();

                try {
                    sock.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                home.close();
                exit();
            }


        });
        home.show();
    }

    // closes the socket
    public void exit() {
        System.exit(0);
    }

    /**
     * Creates new ChatWindow object.
     * @param message a String array, each element of which is a segment from the message received from the server
     */
    public void startChat(String[] message) {
        int ID = Integer.parseInt(message[0]);
        ChatWindow newChat = new ChatWindow(ID);
        if(chatWindows.containsKey(ID)) {
            newChat.setTitle(chatWindows.get(ID).getTitle());
            chatWindows.get(ID).close();
        }
        else
            newChat.setTitle(message);
        newChat.updateChat(message);
        chatWindows.put(ID, newChat);
    }

    // The ChatWindow class represents the user interface through which a user chats with another user. Included are
    // JavaFX elements, functions to send and receive messages, etc.
    class ChatWindow extends Application {

        private int ID;
        private Stage chat;
        private ScrollPane sPane;
        private GridPane convo;
        private int messageNo = 0;

        public ChatWindow(int num) {

            ID = num;
            this.chat = new Stage();
            chat.setTitle("Chat Window");

            convo = new GridPane();

            sPane = new ScrollPane();
            sPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
            sPane.setHbarPolicy(ScrollBarPolicy.NEVER);

            TextField text = new TextField();
            text.setPromptText("Enter message");

            // send a message to another user
            Button send = new Button("Send");
            send.setStyle("-fx-base: " + buttonColor + ";");
            send.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    String s = text.getText();
                    if (s != null) {
                        sendMessage(s);
                        text.clear();
                        text.setPromptText("Enter message");
                    }
                }

            });

            // send message on pressing
            text.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if (keyEvent.getCode() == KeyCode.ENTER)  {
                        String s = text.getText();
                        if (s != null) {
                            sendMessage(s);
                            text.clear();
                            text.setPromptText("Enter message");
                        }
                    }
                }
            });

            VBox box = new VBox();
            GridPane screen = new GridPane();
            screen.getRowConstraints().add(new RowConstraints(270));
            screen.getRowConstraints().add(new RowConstraints(30));
            screen.getColumnConstraints().add(new ColumnConstraints(250));
            screen.getColumnConstraints().add(new ColumnConstraints(50));
            screen.add(sPane, 0, 0);
            screen.add(text,0,1);
            screen.add(send, 1, 1);

            sPane.setContent(convo);
            box.getChildren().addAll(screen);
            Scene scene = new Scene(box, 300, 300);
            sPane.setMinViewportWidth(scene.getWidth()-15);
            chat.setScene(scene);
            chat.show();
        }

        public void close() {
            chat.close();
        }

        /**
         * This function sets the title of a chat window to be the name of the person you're chatting with.
         * @param message a String array, each element of which is a segment from the message received from the server
         */
        public void setTitle(String[] message) {
            String sentMessage = message[2];
            sentMessage = sentMessage.substring(28, sentMessage.length());
            chat.setTitle(sentMessage);
        }

        public void setTitle(String title) {
            chat.setTitle(title);
        }

        public String getTitle() {
            return chat.getTitle();
        }

        @Override
        public void start(Stage arg0) throws Exception {
            //nothing
        }


        public int getID() {
            return ID;
        }

        /**
         * This function updates the chat window on the user's side to display changes received from the server (when the
         * user receives a message or sends a message to other users.
         * @param message a String array, each element of which is a segment from the message received from the server
         */
        public void updateChat(String[] message) {

            // text1 and text2 to allow for boldface username in chat
            TextFlow text = new TextFlow();
            Text text1 = new Text(message[1] + ": " );
            text1.setStyle("-fx-font-weight: bold");
            Text text2 = new Text(message[2]);
            text.getChildren().addAll(text1,text2);

            // start message of each chat is always in black
            if (message[1].equals("CONSOLE")) {
                text1.setFill(Color.BLACK);
                text2.setFill(Color.BLACK);
            }

            // if you are the user that sent the message, text should appear in color selected by the user
            else if(message[1].equals(name)) {
                text1.setFill(mine);
                text2.setFill(mine);
            }

            // else, message is from someone else, text should appear in the color chosen
            else {
                text1.setFill(theirs);
                text2.setFill(theirs);
            }

            text.setMaxWidth(sPane.getWidth());
            text.setBorder(null);
            convo.add(text, 1, messageNo);
            messageNo++;

            sPane.setVvalue(1.0);
        }

        /**
         * This function sends the message the user wants sent to the server, along with other important information.
         * @param message is a String taken from the user's input on what they want to send
         */
        public void sendMessage(String message) {
            writer.println(ID + separator + name + separator + message);
            writer.flush();
        }
    }

    // this class's only job is to poll the server to see if there are any updates made to the Observable
    class IncomingReader implements Runnable {

        @Override
        public void run() {

            String incoming;

            try {
                while ((incoming = reader.readLine()) != null) {

                    // message received from the server
                    String[] message = incoming.trim().split("\\s+");

                    // instruction from server is to update
                    if (message[0].equals("show_online")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                loggedIn(Arrays.copyOfRange(message, 2 , message.length - 1));
                            }
                        });
                    }

                    // server indicates another user wants to either start messaging or send a message to an existing chat
                    else if (isNumeric(message[0])) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {

                                int ID = Integer.parseInt(message[0]);

                                // chat window ID is already contained within this user's list of open chat windows
                                if (chatWindows.containsKey(ID)) {
                                    if(message[1].equals("CONSOLE") && message[2].equals("Chat refresh requested."))
                                        startChat(message);
                                    else
                                        chatWindows.get(ID).updateChat(message);
                                }

                                // new chat being initiated
                                else {
                                    startChat(message);
                                }
                            }
                        });
                    }

                    // server telling client username is taken, already exists
                    else if (message[0].equals("USEREXISTS")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                userExists(message);
                            }
                        });
                    }

                    // server telling client user is already logged in
                    else if(message[0].equals("ALREADYLOGGEDIN")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                alreadyLogged(message);
                            }
                        });
                    }

                    // server telling client the entered password is incorrect
                    else if (message[0].equals("WRONGPASS")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                wrongPass(message);
                            }
                        });
                    }
                }
            } catch (IOException ex) {
                if(ex instanceof SocketException) {}
                else ex.printStackTrace(); 	}
        }
    }

    /**
     * Determines if an input is a number
     * @param str is a String input
     * @return boolean true if input is numeric, false if not
     */
    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) { e.printStackTrace(); }

    }



}