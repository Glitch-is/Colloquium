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
	private String MOTD = " -- <b>Welcome to Colloquium</b>\n -- <b>Check us out on GitHub https://github.com/Glitch-is/Colloquium </b>\n -- <b>Authors: Glitch & Index</b>";
	private static Map<String, ChatRoom> chatRoom = new HashMap<String, ChatRoom>();
	private static Map<String, User> users = new HashMap<String, User>();	
	
	public synchronized void connect(User user)
	{
		users.put(user.getNick(), user);
	}

	public synchronized void disconnect(String user) throws IOException
	{
		if(users.containsKey(user))
		{
			for(String c : users.get(user).getChatrooms())
			{
				chatRoom.get(c).leave(user, true);
			}
			users.remove(user);
		}
		else
		{
			for(Map.Entry<String, ChatRoom> chatr : chatRoom.entrySet())
			{
				if(chatr.getValue().containsUser(user))
				{
					System.out.println(user + " leaving " + chatr.getKey());
					chatr.getValue().leave(user, true);
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

	public synchronized void addRoom(String name)
	{
		chatRoom.put(name, new ChatRoom(name));
	}

	public synchronized ChatRoom getRoom(String name)
	{
		return chatRoom.get(name);
	}

	public synchronized void join(String chan, User user) throws IOException
	{
		chan = escapeHTML(chan);
		if(!chatRoom.containsKey(chan))
		{
			addRoom(chan);
			getRoom(chan).setPrivilege(user.getNick(), "Owner");
		}
		getRoom(chan).join(user);
	}

	public synchronized static User getUser(String name)
	{
		return users.get(name);
	}

	public synchronized boolean contains(String name)
	{
		return users.containsKey(name);
	}

	public synchronized static Map<String, User> getUsers()
	{
		return users;	
	}

	public synchronized static void setUser(User user)
	{
		users.put(user.getNick(), user);
	}

	public synchronized static void remUser (String nick)
	{
		users.remove(nick);
	}

	public synchronized static String listUsers ()
	{
		String ret =  "[";
		for (Map.Entry<String, User> u : users.entrySet())
		{
		
			ret += u.getKey() + ",";
		}
		return ret.substring(0, ret.length()-1) + "]";
		
	}
}

