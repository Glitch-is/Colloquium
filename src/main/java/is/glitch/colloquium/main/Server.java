/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is.glitch.colloquium.main;

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

	public void addRoom(String name)
	{
		chatRoom.put(name, new ChatRoom(name));
	}

	public ChatRoom getRoom(String name)
	{
		return chatRoom.get(name);
	}

	public void join(String chan, User user) throws IOException
	{
		if(!chatRoom.containsKey(chan))
		{
			addRoom(chan);
			getRoom(chan).setPrivilege(user.getNick(), "Owner");
		}
		getRoom(chan).join(user);
	}

	
}

