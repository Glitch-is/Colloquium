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
	private String nick;
	private ArrayList<String> chatrooms = new ArrayList<String>();
	private Session sesh;
	
	public User(String n, Session s)
	{
		this.nick = n;
		this.sesh = s;
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
		nick = escapeHTML(nick);
		this.nick = nick;
	}

	/**
	 * @return the sesh
	 */
	public Session getSesh() {
		return sesh;
	}

	public void send(String head, String message) throws IOException
	{
		sesh.getBasicRemote().sendText("{\"head\":\""+head+"\", \"chatroom\":\"\", \"message\":\""+message+"\"}");
	}

	public void join(String name)
	{
		chatrooms.add(name);
	}

	public void leave(String name)
	{
		chatrooms.remove(name);
	}


	public ArrayList<String> getChatrooms()
	{
		return chatrooms;
	}
	
}
