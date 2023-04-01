import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Main {
	public static void main(String[] args) {
		//String jobID = ""; // ID
		String username = System.getProperty("user.name");
		String str = "";
		/*boolean hasServer = false;
		String serverType = ""; // Largest server type
		
		int currID = 0;
		int highestID = 0;
		int core = 0;*/
		try {
			Socket s = new Socket("localhost", 50000);
			System.out.println("Server connected.");
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			Client c = new Client(dout, in, username);
			
			c.handshake();
			
			c.sendOut("REDY");
			c.receiveIn(); //get job data
			
			c.getLargestServer();
			System.out.println(c.serverType);
			System.out.println(c.highestID);
			
			c.sendOut("QUIT");
			dout.close();
			s.close();
		}
		catch (Exception e) {System.out.println(e);}
		
	}
	
	/*private static String getJob(DataOutputStream dout, BufferedReader in) {
		try {
			String str = (String)in.readLine();
			System.out.println(str);
			// Need to get core memory disk from JOBN message	  
			String[] buffer = str.split(" ", 4);
			System.out.println("Buffer: " + buffer[2]);
			String jobID = buffer[2];
			
			return jobID;
		}
		catch (Exception e) {System.out.println(e); return "";}
	}*/
}

class Client {
	private String jobID = ""; // ID
	private String username;
	public String serverType = ""; // Largest server type
	private int currID = 0;
	public int highestID = 0;
	private int core = 0;
	private DataOutputStream dout;
	private BufferedReader in;
	// Last received message
	public String prevMessage = "";
	
	public Client(DataOutputStream x, BufferedReader y, String uname) {
		dout = x;
		in = y;
		username = uname;
	}
	
	public void sendOut(String command) {
		try {
			dout.write((command + "\n").getBytes());
			dout.flush();
		}
		catch (Exception e) {}
	}
	
	public void receiveIn() {
		try {
			prevMessage = (String)in.readLine();
		}
		catch (Exception e) {}
	}
	
	public void handshake() {
		try {
			sendOut("HELO");
			receiveIn();
			sendOut("AUTH " + username);
			receiveIn();
		}
		catch (Exception e) {}
	}
	
	//public void getJob();
	
	public void getLargestServer() {
		try {
			sendOut("GETS All");
			// DATA nServer
			receiveIn();
			String[] buffer = prevMessage.split(" ", 3);
			int serverNum = Integer.parseInt(buffer[1]);
			sendOut("OK");
			// find server.
			for (int i = 0; i < serverNum; i++) {
				// find largest server type and largest index based on cores (serverType serverID status something core)
				receiveIn();
				buffer = prevMessage.split(" ", 6); //todo
				// if buffer core > core than change serverType, set highest core, set highestID to 0	
				if (Integer.parseInt(buffer[4]) > core) {
					serverType = buffer[0];
					core = Integer.parseInt(buffer[4]);
					highestID = 0;
				}
				else if (buffer[0].equals(serverType)) {
					highestID++;
				}
			} 
			sendOut("OK");
		}
		catch (Exception e) {}
	}

}
