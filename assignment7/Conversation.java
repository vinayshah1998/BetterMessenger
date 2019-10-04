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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

public class Conversation {


    String id;
    ArrayList<String> members;
    ArrayList<String> messages;

    public Conversation() {
        id = UUID.randomUUID().toString();
        members = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public Conversation(String id, ArrayList<String> members) {
        this.id = id;
        this.members = members;
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public void removeMember(String member) { members.remove(member); }

    public void clearMembers() {
        members.clear();
    }

    public void addMessage(String message) {
        messages.add(message);
    }
}
