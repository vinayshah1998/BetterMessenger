package assignment7;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Observable;

public class ClientThread extends Observable implements Runnable {
    /** For reading input from socket */
    private BufferedReader br;

    /** For writing output to socket. */
    private PrintWriter pw;

    /** Socket object representing client connection */

    private Socket socket;
    private boolean running;

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        running = false;
        //get I/O from socket
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
            running = true; //set status
        }
        catch (IOException ioe) {
            throw ioe;
        }
    }

    /**
     *Stops clients connection
     */

    public void stopClient()
    {
        try {
            this.socket.close();
        }catch(IOException ioe){ };
    }

    public void run() {
        String msg = ""; //will hold message sent from client

        //sent out initial welcome message etc. if required...

        pw.println("Welcome to Java based Server");
        pw.println("Would you like to do group chat or direct message?");


        //start listening message from client//

        try {
            while (!((msg = br.readLine()).equalsIgnoreCase("group") || (msg = br.readLine()).equalsIgnoreCase("direct"))){
            }
            if (msg.equalsIgnoreCase("group")){
                pw.println("Welcome to the group chat");
            } else if (msg.equalsIgnoreCase("direct")){

            }


            while ((msg = br.readLine()) != null && running) {
                //provide your server's logic here//

                //right now it is acting as an ECHO server//

                pw.println(msg); //echo msg back to client//
            }
            running = false;
        }
        catch (IOException ioe) {
            running = false;
        }
        //it's time to close the socket
        try {
            this.socket.close();
            System.out.println("Closing connection");
        } catch (IOException ioe) { }

        //notify the observers for cleanup etc.
        this.setChanged();              //inherit from Observable
        this.notifyObservers(this);     //inherit from Observable
    }
}
