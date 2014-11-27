/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is.glitch.colloquium.main;

import static is.glitch.colloquium.main.Utils.escapeHTML;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author glitch
 */
public class Server {
	private String MOTD = " -- <b>Welcome to Colloquium</b>\n -- <b>Check us out on GitHub https://github.com/Glitch-is/Colloquium </b>\n -- <b>Authors: Glitch & Index</b>"; // Our server's message of the day
	private static Map<String, ChatRoom> chatRoom = new HashMap<String, ChatRoom>(); // All the channels on the server
	private static Map<String, User> users = new HashMap<String, User>();	// List of all the users connected to the server
	
	public synchronized void connect(User user) // Connect the user to the server
	{
		users.put(user.getNick(), user); // Add the user to the list of connected users
	}

	public synchronized void disconnect(String user) throws IOException // Disconnect from the server
	{
		if(users.containsKey(user)) // Check if the user is connected
		{
			for(String c : users.get(user).getChatrooms()) // Loop through all of the user's channels
			{
				chatRoom.get(c).leave(user, true); // Leave channel
			}
			users.remove(user); // Remove the user from the server
		}
		else // This can be used as a ghost command
		{
			for(Map.Entry<String, ChatRoom> chatr : chatRoom.entrySet()) // Loop through every channel on the server
			{
				if(chatr.getValue().containsUser(user)) // Check if said user is connected
				{
					System.out.println(user + " leaving " + chatr.getKey()); // Print out the leaving messaage
					chatr.getValue().leave(user, true); // Remove the user from the channel
				}
			}
		}
	}

	/**
	 * @return the MOTD
	 */
	public String getMOTD() {
		return MOTD;
	}

	/**
	 * @param MOTD the MOTD to set
	 */
	public void setMOTD(String MOTD) {
		this.MOTD = MOTD;
	}

	public synchronized void addRoom(String name) // To create a room
	{
		chatRoom.put(name, new ChatRoom(name)); // Add the channel to the list of channels
	}

	public synchronized ChatRoom getRoom(String name) // return the chatrom object
	{
		return chatRoom.get(name);
	}

	public synchronized void join(String chan, User user) throws IOException // To join a channel
	{
		chan = escapeHTML(chan); // Escape any html that might be in the name
		if(!chatRoom.containsKey(chan)) // Check if it already exists
		{
			addRoom(chan); // if not, create it
			getRoom(chan).setPrivilege(user.getNick(), "Owner"); // Set that user as the owner
		}
		getRoom(chan).join(user); // Join the user
	}

	public synchronized static User getUser(String name) // Get the user object
	{
		return users.get(name);
	}

	public synchronized boolean contains(String name) // Check if the user is connected to the server
	{
		return users.containsKey(name);
	}

	public synchronized static Map<String, User> getUsers() // Get all of the users
	{
		return users;	
	}

	public synchronized static void setUser(User user) // Add the user to the server
	{
		users.put(user.getNick(), user);
	}

	public synchronized static void remUser (String nick) // Remove the user from the server
	{
		users.remove(nick);
	}

	public synchronized static String listUsers () // Create a list of all the connected users
	{
		String ret =  "["; // We're manually creating the JSON
		for (Map.Entry<String, User> u : users.entrySet()) // Loop through all the users
		{
			ret += u.getKey() + ","; // Add the user and comma seperate
		}
		return ret.substring(0, ret.length()-1) + "]"; // Return the finished list
		
	}
}

