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

			DataInputStream fromServer = new DataInputStream(connectToServer.getInputStream());

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

            ColloquiumListen listen = new ColloquiumListen(fromServer);
            listen.start();
            ColloquiumSend send = new ColloquiumSend(toServer);
            send.start();


		} catch (IOException ex) {
			System.err.println(ex);
		}

	}

}

class ColloquiumListen extends Thread {
    public DataInputStream i;

    public ColloquiumListen(DataInputStream inp){
        this.i = inp;
    }

    @Override
    public void run(){
        try
        {
            String output = "";
            while(true)
            {
                output = i.readUTF();
                System.out.println(output);
            }
        }
        catch(IOException ex)
        {

        }
    }
}

class ColloquiumSend extends Thread {
    private DataOutputStream o;
    public ColloquiumSend(DataOutputStream outp){
        this.o = outp;
    }

    @Override
    public void run(){
        try
        {
            Scanner reader = new Scanner(System.in);
            while(true)
            {
                String input = reader.next();
                o.writeUTF(input);
            }
        }
        catch(IOException ex)
        {

        }
    }
}
