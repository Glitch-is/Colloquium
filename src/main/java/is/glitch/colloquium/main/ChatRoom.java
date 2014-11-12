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
	private String name;	
	private Map<String, String> privilege = new HashMap<String, String>();
	private List<String> users = new ArrayList<String>();
	private JSONArray editor = new JSONArray();

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
		for(String u : users)	
		{
			ret += (ret.length() == 1 ? "" : ",") + "[\"" + u +"\",\""+ checkPrivilege(u) + "\"]";
		}
		return ret + "]";
	}

	public String getUsers(){
		return Integer.toString(users.size());
	}

	public void join(User user) throws IOException{
		users.add(user.getNick());
		user.join(name);
		updateNicklist();
		sendAll("server", "\"<span style='color: white'><b>" + user.getNick() + "</b></span> has joined <span style='color: white'><b>#" + name + "</b></span>\"");
		user.send("editor", name, editor.toString());
	}

	public void updateNicklist() throws IOException
	{
		sendAll("nicklist", getNickList());
	}

	public void leave(String nick) throws IOException
	{
		if(users.contains(nick))
		{
			getUser(nick).leave(name);
			users.remove(nick);
			sendAll("server", "\"<span style='color: gray'><b>" + nick + "</b></span> has left <span style='color: gray'><b>#" + name + "</b></span>\"");
		}
		updateNicklist();
	}

	public void rem(String nick)
	{
		users.remove(nick);
	}

	public void put(String nic)
	{
		users.add(nic);
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
		String s[] = message.substring(1, message.length()-1).split("\n");
		if(s.length > 1)
		{
			for(String l : s)
				sendAll(nick, "\"" + l + "\"");
		}
		else
		{
			for(String u : users)	
			{
				User user = getUser(u);
				user.getSesh().getBasicRemote().sendText("{\"head\":\""+nick+"\", \"chatroom\":\""+name+"\", \"message\":"+message+"}");
			}
		}
	}

	public boolean containsUser(String nick)
	{
		return users.contains(nick);
	}

	/**
	 * @return the editor
	 */
	public JSONArray getEditor() {
		return editor;
	}

	/**
	 * @param editor the editor to set
	 */
	public void setEditor(JSONArray editor) throws IOException {
		this.editor = editor;
		sendAll("editor", editor.toString());
	}
}
