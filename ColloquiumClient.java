import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ColloquiumClient {

	public static void main(String[] args) {
		try {
			//Socket
			Socket connectToServer = new Socket("localhost",7331);
			
			//Frá server
			DataInputStream fromServer = new DataInputStream(connectToServer.getInputStream());
			
			//Til server
			DataOutputStream toServer = new DataOutputStream(connectToServer.getOutputStream());
			
			//Reader
			Scanner reader = new Scanner(System.in);
			
			//MOTD
			String motd = fromServer.readUTF();
			System.out.println(motd);
			
			String nick = "";
			
			while(nick.equals("")){
				System.out.print("Nick: ");
				nick = reader.next();
			}
			
			toServer.writeUTF(nick);
			String nickout = fromServer.readUTF();
			System.out.println(nickout);
			
			while(true){
				
				

				String input = reader.next();
				// Senda til server
				System.out.print("Me>");
				toServer.writeUTF(input);
				
				//Taka á móti frá server
				String output = fromServer.readUTF();
				
				
				System.out.println(output);
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
		}

	}

}
