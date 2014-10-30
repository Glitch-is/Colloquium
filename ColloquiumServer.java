import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ColloquiumServer {
	
	public static String MOTD = "Test";
	public static Map<String, Chatroom> chatrooms = new HashMap<String, Chatroom>();

	public static void main(String[] args) {
		int portNumber = 7331;
		try {
			
			// Socket
			ServerSocket serverSocket = new ServerSocket(portNumber);
			
			Map<String, Chatroom> chatrooms = new HashMap<String, Chatroom>();
			chatrooms.put("Test", new Chatroom("Test"));
			
			while(true){
				User user = new User(serverSocket.accept());
				user.start();
				
				chatrooms.get("Test").Users.put(user.Nick, user);
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
	
}

class User extends Thread{
	
	public String Nick;
	private Socket Sock;
	public ArrayList<String> Output = new ArrayList<String>();
	
	public User(Socket sock) {
		this.Sock = sock;
	}
	
	@Override
	public void run(){
		try {
			//Frá client
			DataInputStream fromClient = new DataInputStream(Sock.getInputStream());
			
			//Til client
			DataOutputStream toClient = new DataOutputStream(Sock.getOutputStream());
			
			//MOTD
			toClient.writeUTF(ColloquiumServer.MOTD);
			
			//Nickname
			String nikput = fromClient.readUTF();
			this.Nick = nikput;
			
			toClient.writeUTF("Welcome " + this.Nick);
			
			while(true){
				// Lesa frá client
				String input = fromClient.readUTF();
				
				ColloquiumServer.chatrooms.get("Test").sendMessage(input);
				
				while(!this.Output.isEmpty()){
					toClient.writeUTF(this.Output.get(0));
					this.Output.remove(0);
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}

class Chatroom {
	
	private String Name;
	public Map<String, User> Users = new HashMap<String, User>();
	
	public Chatroom(String name){
		this.Name = name;
	}
	
	public void sendMessage(String message){
		for(Map.Entry<String, User> u : Users.entrySet()){
			u.getValue().Output.add(message);
		}
	}
	
}

