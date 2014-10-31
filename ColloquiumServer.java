import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ColloquiumServer {

	public static String MOTD = "Welcome to Colloquium\n";
	public static Map<String, Chatroom> chatrooms = new HashMap<String, Chatroom>();

	public static void main(String[] args) {
		int portNumber = 7331;
		try {
            System.out.println("Starting Colloquium Server...");

			ServerSocket serverSocket = new ServerSocket(portNumber);

			chatrooms.put("Test", new Chatroom("Test"));

            System.out.println(chatrooms);

            System.out.println("Server Started!");

			while(true){
				Thread user = new Thread(new User(serverSocket.accept()));
				user.start();
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

}

class User implements Runnable{

	public String Nick;
	private Socket Sock;
	public ArrayList<String> Output = new ArrayList<String>();

	public User(Socket sock) {
		this.Sock = sock;
	}

	@Override
	public void run(){
		try {
			DataInputStream fromClient = new DataInputStream(Sock.getInputStream());

			DataOutputStream toClient = new DataOutputStream(Sock.getOutputStream());

			//MOTD
			toClient.writeUTF(ColloquiumServer.MOTD);

			//Nickname
			String nikput = fromClient.readUTF();
			Nick = nikput;

			toClient.writeUTF("Welcome " + Nick);

            System.out.println(Nick + " Joined");

            Join("Test");

			while(true){
				String input = fromClient.readUTF();

				ColloquiumServer.chatrooms.get("Test").sendMessage(input, Nick);

				while(!Output.isEmpty()){
					toClient.writeUTF(Output.get(0));
					Output.remove(0);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

    public void Join(String chan){
        // Check if channel exists
        // If not... make it
        ColloquiumServer.chatrooms.get(chan).Users.put(Nick, this);
    }

}

class Chatroom {

	private String Name;
	public Map<String, User> Users = new HashMap<String, User>();

	public Chatroom(String name){
		this.Name = name;
	}

	public void sendMessage(String message, String nick){
		for(Map.Entry<String, User> u : Users.entrySet()){
			u.getValue().Output.add(nick + "> " + message);
            System.out.println("[#"+Name+"] "+nick+"> ");
		}
	}

}

