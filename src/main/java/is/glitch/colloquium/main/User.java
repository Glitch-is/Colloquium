/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is.glitch.colloquium.main;

import static is.glitch.colloquium.main.Utils.escapeHTML;
import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.Session;

/**
 *
 * @author glitch
 */
public class User {
	private String nick; // The user's nickname
	private ArrayList<String> chatrooms = new ArrayList<String>(); // List of all the user's connected channels
	private Session sesh; // The user's websocket connection
	
	public User(String n, Session s) //Constructor
	{
		this.nick = n; // Assign the user's nickname
		this.sesh = s; // Assign the user's session
	}

	/**
	 * @return the nick
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick the nick to set
	 */
	public void setNick(String nick) {
		nick = escapeHTML(nick); // Make sure to escape any HTML
		this.nick = nick;
	}

	/**
	 * @return the sesh
	 */
	public Session getSesh() {
		return sesh;
	}

	public void send(String head, String chan, String message) throws IOException // Send the user a message
	{
		String s[] = message.substring(1, message.length() - 1).split("\n"); // Split the string up if it's multiple lines
		if(s.length > 1) // If it is multiple lines
		{
			for(String l : s) // Loop though the lines
				send(head, chan, "\"" + l + "\""); // Send the line to the user
		}
		else
			sesh.getBasicRemote().sendText("{\"head\":\""+head+"\", \"chatroom\":\""+chan+"\", \"message\":"+message+"}"); // Send the user the message
	}

	public synchronized void join(String name) // Join a channel
	{
		chatrooms.add(name); // Add the channel to the user's list of connected channels
	}

	public synchronized void leave(String name) // Leave a channel
	{
		chatrooms.remove(name); // remove channel from the list
	}


	public synchronized ArrayList<String> getChatrooms() // Return all of the channels the user is connected to
	{
		return chatrooms;
	}
	
}
