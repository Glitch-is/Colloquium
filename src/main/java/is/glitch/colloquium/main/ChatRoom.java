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
public class ChatRoom {
	private String name;	
	private Map<String, String> privilege = new HashMap<String, String>();
	private static Map<String, User> users = new HashMap<String, User>();

	public ChatRoom(String n)
	{
		this.name = n;
	}

	public String checkPrivilege(String nick)
	{
		if(privilege.containsKey(nick))
			return privilege.get(nick);
		else
			return "User";
	}

	public void setPrivilege(String nick, String priv)
	{
		privilege.put(nick, priv);
	}

	public String getNickList(){
		String ret = "[";
		for(Map.Entry<String, User> u : users.entrySet())	
		{
			ret += (ret.length() == 1 ? "" : ",") + "[\"" + u.getKey() +"\",\""+ checkPrivilege(u.getKey()) + "\"]";
		}
		return ret + "]";
	}

	public String getUsers(){
		return Integer.toString(users.size());
	}

	public void join(User user) throws IOException{
		users.put(user.getNick(), user);
		user.join(name);
		updateNicklist();
		sendAll("server", "\"<span style='color: white'><b>" + user.getNick() + "</b></span> has joined <span style='color: white'><b>#" + name + "</b></span>\"");
	}

	public void updateNicklist() throws IOException
	{
		sendAll("nicklist", getNickList());
	}

	public void leave(String nick) throws IOException
	{
		if(users.containsKey(nick))
		{
			users.get(nick).leave(name);
			users.remove(nick);
			sendAll("server", "\"<span style='color: gray'><b>" + nick + "</b></span> has left <span style='color: gray'><b>#" + name + "</b></span>\"");
		}
		updateNicklist();
	}

	public void rem(String nick)
	{
		users.remove(nick);
	}

	public void put(String nick, User usr)
	{
		users.put(nick, usr);
	}

	public void remPriv(String nick)
	{
		privilege.remove(nick);
	}

	public void putPriv(String nick, String priv)
	{
		privilege.put(nick, priv);
	}

	public void sendAll(String nick, String message) throws IOException
	{
		for(Map.Entry<String, User> u : users.entrySet())	
		{
			u.getValue().getSesh().getBasicRemote().sendText("{\"head\":\""+nick+"\", \"chatroom\":\""+name+"\", \"message\":"+message+"}");
		}
	}

	public boolean containsUser(String nick)
	{
		return users.containsKey(nick);
	}
}
