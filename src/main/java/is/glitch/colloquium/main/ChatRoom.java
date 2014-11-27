/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is.glitch.colloquium.main;

import static is.glitch.colloquium.main.Server.getUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

/**
 *
 * @author glitch
 */
public class ChatRoom {
	private String name; // The name of the channel
	private Map<String, String> privilege = new HashMap<String, String>(); // Store the privileges of the channel
	private List<String> users = new ArrayList<String>(); //All of the connected users
	private JSONArray editor = new JSONArray(); // The editor for the channel

	public ChatRoom(String n)  // Constructor
	{
		this.name = n; // Set the name of the channel
	}

	public String checkPrivilege(String nick) // Check privileges for a given user
	{
		if(privilege.containsKey(nick)) // Check if the user is in the list
			return privilege.get(nick); // Return his status
		else
			return "User"; // If he's not on the list he's just a regular user
	}

	public void setPrivilege(String nick, String priv) // Set a user's privileges
	{
		privilege.put(nick, priv);  // Add him to the list with the given privilege
	}

	public String getNickList(){ // Get a JSON string of all the users and their privileges
		String ret = "["; // We're creating the JSON manually
		for(String u : users) // Loop through all of the users on the channel
		{
			ret += (ret.length() == 1 ? "" : ",") + "[\"" + u +"\",\""+ checkPrivilege(u) + "\"]"; // Add the user to the list with his privilege
		}
		return ret + "]"; // Return the finished JSON 
	}

	public String getUsers(){ // Get the amount of users connected to the channel
		return Integer.toString(users.size());
	}

	public synchronized void join(User user) throws IOException{ // User joins the channel
		users.add(user.getNick()); // Add the user to the list of users
		user.join(name); // add the channel to the user's list of connected channels
		updateNicklist(); // Update the nickname list
		sendAll("server", "\"<span style='color: white'><b>" + user.getNick() + "</b></span> has joined <span style='color: white'><b>#" + name + "</b></span>\""); // Tell everyone who joined
		user.send("editor", name, editor.toString()); // send the newly connected user what's in the editor
	}

	public void updateNicklist() throws IOException
	{
		sendAll("nicklist", getNickList()); // Send everyone the updated nickname list
	}

	public synchronized void leave(String nick, boolean purge) throws IOException // Leave the channel
	{
		if(users.contains(nick)) // Check if the user is connected
		{
			if(!purge) //Check if we just want to remove him from the channel or remove the channel from his list as well (Used if the user doesnt' exist) GHOSTS!
				getUser(nick).leave(name); // Remove the channel from the user's list (Needs to exists)
			users.remove(nick); // Remove the user from the channel
			sendAll("server", "\"<span style='color: gray'><b>" + nick + "</b></span> has left <span style='color: gray'><b>#" + name + "</b></span>\""); // Tell everyone who left
		}
		updateNicklist(); // Update the nickname list
	}

	public synchronized void rem(String nick) //Remove ther user from the channel
	{
		users.remove(nick);
	}

	public synchronized void put(String nic) // Add the user to the channel
	{
		users.add(nic);
	}

	public void remPriv(String nick) // Remove the privileges from the user
	{
		privilege.remove(nick);
	}

	public void putPriv(String nick, String priv) // Add privileges to the user
	{
		privilege.put(nick, priv);
	}

	public void sendAll(String nick, String message) throws IOException // Send everyone connected to the channal a message
	{
		String s[] = message.substring(1, message.length()-1).split("\n"); // Split up the string if there are multiple lines
		if(s.length > 1) // Check if there' are multiple lines
		{
			for(String l : s) // Loop though all of the lines
				sendAll(nick, "\"" + l + "\""); // Send the line to the channel
		}
		else
		{
			for(String u : users) // Loop through all of the users
			{
				User user = getUser(u); // Get the object of the user
				if(user != null) // Check if the user exists (This is messing something up I think)
				{
					user.getSesh().getBasicRemote().sendText("{\"head\":\""+nick+"\", \"chatroom\":\""+name+"\", \"message\":"+message+"}"); // Send the user the message
				}
			}
		}
	}

	public synchronized boolean containsUser(String nick) // Check whether the user is connected to te channel or not
	{
		return users.contains(nick);
	}

	/**
	 * @return the editor
	 */
	public synchronized JSONArray getEditor() {
		return editor;
	}

	/**
	 * @param editor the editor to set
	 * @param sender
	 */
	public synchronized void setEditor(JSONArray editor, String sender) throws IOException {
		this.editor = editor; // Assign the new object to the editor
		for(String u : users) // Loop through the users
		{
			if(u == null ? sender != null : !u.equals(sender)) // Don't send the user the updated editor, really buggy client side
			{
				User user = getUser(u); // Get the user object
				user.getSesh().getBasicRemote().sendText("{\"head\":\"editor\", \"chatroom\":\""+name+"\", \"message\":"+this.editor.toString()+"}"); // Send the upated editor
			}
		}
	}
}
