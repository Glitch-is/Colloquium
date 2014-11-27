package is.glitch.colloquium.main;

import static is.glitch.colloquium.main.Utils.escapeHTML;
import static is.glitch.colloquium.main.Server.setUser;
import static is.glitch.colloquium.main.Server.remUser;
import java.io.IOException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.*;

// Our main server endpoint
@ServerEndpoint("/")
public class Main {
	private static final Server serv = new Server(); // Create a new instance of a server (I guess we could have multiple virtual servers) Note these are static
	private static int userCount = 0; // Just to keep track of how many users we have
	private User user; // None-static user variable which gets created each time a new connection is made


	// Catch all the errors
    @OnError
    public void onError(Throwable t) throws IOException {
	    //Quit(); // Do better Error handling
	    t.printStackTrace();
    }

    //When the user connects to our server
    @OnOpen
    public void Init(Session sesh) throws IOException{
	user = new User("user" + ++userCount, sesh); // Create a new user for him
	user.send("motd", "", "\"" + serv.getMOTD() + "\""); // Send him the message of the day
	serv.connect(user); // and finally connect him to the serve
	serv.join("main",user); // Force him to join the main channel
    }
    
    //When the user disconnects
    @OnClose
    public void Quit() throws IOException{
	serv.disconnect(user.getNick()); // We disconnect him from our server
    }
    
    //When the user send any kind of message
    @OnMessage
    public void Message(String message) throws IOException {
	    JSONObject obj = new JSONObject(message); // Since the message is JSON we need to get it into a JSONObject
	    /*
	    	Our messages are structured as follows
	    	Head -- Type of message (private, editor...)
	        ChatRoom -- Specifies what chatroom the message is suppose to go to
	        Message -- The contents of the message
	    */
	    switch(obj.getString("head")) // Make cases for our head
	    {
		    case "message": // regular message
			serv.getRoom(obj.getString("chatroom")).sendAll(user.getNick(), "\"" + escapeHTML(obj.getString("message")) + "\""); //Send to everyone in channel
		    break;
		    case "nick": // Nickname change
			    String old = user.getNick(); // Store his old username
			    String n = obj.getString("message"); //get his new one
			    if(n.length() > 30) // Make sure it's less that 30 characters
				    break;
			    if(!serv.contains(n)) // Check if it doesn't already exist
			    {
				    for(String chan : user.getChatrooms()) // Loop through all of the users connected channels
				    {
					ChatRoom room = serv.getRoom(chan); // Get the object for the channel
					String priv = room.checkPrivilege(old); // Check his privileges in the channel so he can keep them
					room.sendAll("server", "\"<span style='color: gray;'><b>" + old + "</b></span> is now known as <span style='color: white'><b>" + n + "</b></span>\""); // Send everyone the new nickname
					room.rem(old); // Remove the old nickname
					room.put(n); // Add the new one
					room.remPriv(old); // remove the old privileges
					room.putPriv(n, priv); // Add the new privileges
					room.updateNicklist(); // Send everyone an updated nickname list (Doesn't send to the initial user for some reason....)
				    }
				    remUser(user.getNick()); // Remove the old user from the server
				    user.setNick(obj.getString("message")); // Change the nickname of the user
				    setUser(user); // Add the new user to the server
			    }
			    else
			    {
				user.send("server", "","\"Nick <span style='color: gray'>" + n + "</span> is already in use\""); // Tell him if it's taken
			    }
		    break;
		    case "editor":
			    ChatRoom channel = serv.getRoom(obj.getString("chatroom")); // get the channel object
			    if(channel != null) // Check if the channel exists
				    channel.setEditor(obj.getJSONArray("message"), user.getNick()); // Update the editor
			    else //Otherwise we assume it's for a private message
			    {
				    //Need to create new instances of editor for PM's
			    }
			    break;
		    case "join":
			    String chanName = obj.getString("message"); // Get the name of the channel to join
			    serv.join(chanName, user); // make the user join said channel
			    break;
		    case "private":
			    if(serv.contains(obj.getString("chatroom"))) // Check if the user exists
			    {
				    Server.getUser(obj.getString("chatroom")).send("private", user.getNick(), "\"" + user.getNick() + " " + obj.getString("message") + "\""); // Send the reciever the message
				    user.send("private", obj.getString("chatroom"), "\"" + user.getNick() + " " + obj.getString("message") + "\""); //Send the sender the message
			    }
			    else
			    {
				    user.send("server", "", "\"<span style='color: gray'>" + obj.getString("chatroom") + "</span>: No such nick/channel\""); // Tell the user said user doesn't exist
			    }
			    break;
		    case "action":
			    serv.getRoom(obj.getString("chatroom")).sendAll("action", "\"" + user.getNick() + " " + obj.getString("message") + "\""); // Send the action to the channel
			    break;
		    case "leave":
			    serv.getRoom(obj.getString("message")).leave(user.getNick(), false); // Leave the channel
			    user.leave(obj.getString("message")); // remove the channel from the user's list as well
			    break;
		    case "marco": // Sort of a ping pong thing...
			    user.send("polio", "", "");
	    }
    }


}

