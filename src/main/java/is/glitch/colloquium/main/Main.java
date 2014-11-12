package is.glitch.colloquium.main;

import static is.glitch.colloquium.main.Utils.escapeHTML;
import java.io.IOException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.*;

@ServerEndpoint("/")
public class Main {

	private static final Server serv = new Server();
	private static int userCount = 0;
	private User user;



    @OnOpen
    public void Init(Session sesh) throws IOException{
	user = new User("user" + ++userCount, sesh);
	user.send("motd", "", "\"" + serv.getMOTD() + "\"");
	serv.join("main",user);
	// Start to ping
    }
    
    @OnClose
    public void Quit() throws IOException{
	for(String chan: user.getChatrooms())	
	{
		serv.getRoom(chan).leave(user.getNick());
	}
    }
    
    @OnMessage
    public void Message(String message) throws IOException {
	    JSONObject obj = new JSONObject(message);
	    switch(obj.getString("head"))
	    {
		    case "message":
			serv.getRoom(obj.getString("chatroom")).sendAll(user.getNick(), "\"" + escapeHTML(obj.getString("message")) + "\"");
		    break;
		    case "nick":
			    String old = user.getNick();
			    String n = obj.getString("message");
			    if(serv.contains(n))
			    {
				    for(String chan : user.getChatrooms())
				    {
					ChatRoom room = serv.getRoom(chan);
					String priv = room.checkPrivilege(old);
					room.sendAll("server", "\"<span style='color: gray;'><b>" + old + "</b></span> is now known as <span style='color: white'><b>" + n + "</b></span>\"");
					room.rem(old);
					room.put(n);
					room.remPriv(old);
					room.putPriv(n, priv);
					room.updateNicklist();
				    }
				    user.setNick(obj.getString("message"));
			    }
			    else
			    {
				user.send("server", "","\"Nick <span style='color: gray'>" + n + "</span> is already in use\"");
			    }
		    break;
		    case "editor":
			    serv.getRoom(obj.getString("chatroom")).setEditor(obj.getJSONArray("message"));
			    break;
		    case "join":
			    String chanName = obj.getString("message");
			    serv.join(chanName, user);
			    break;
		    case "private":
			    
			    break;
		    case "action":
			    serv.getRoom(obj.getString("chatroom")).sendAll("action", "\"" + user.getNick() + " " + obj.getString("message") + "\"");
			    break;
		    case "leave":
			    serv.getRoom(obj.getString("message")).leave(user.getNick());
			    user.leave(obj.getString("message"));
			    break;
		    case "pong":
			    // make sure user doesnt get kicked out
			    break;
	    }
    }
}

