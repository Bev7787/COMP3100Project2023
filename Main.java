import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.HashMap;

public class Main {
	public static void main(String[] args) {
		String username = System.getProperty("user.name");
		String redyResponse = "";
		try {
			Socket s = new Socket("localhost", 50000);
			//System.out.println("Server connected.");
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			Client c = new Client(dout, in, username);
			
			c.handshake();
			
			// FF Algorithm
			while (!(c.prevMessage.equals("NONE"))) {
				c.sendOut("REDY");
				c.receiveIn();
				// Store data for future reference. Split job data into core/memory/disk requirements
				redyResponse = c.prevMessage;
				if (c.getResponseHeader(redyResponse).equals("JOBN")) {
					String[] job = redyResponse.split(" ", 5);
					// Get the first available server. If none are available, find the first capable server.
					c.getFirstAvailServer(job[4]);
					if (!(c.isAvailable)) {
						//System.out.println("job: " + redyResponse);
						c.getFirstCapableServer(job[4]);
					}
		

					c.scheduleJob(redyResponse);
					c.receiveIn();
				}
			}
			c.sendOut("QUIT");
			c.receiveIn();
			dout.close();
			s.close();
		}
		catch (Exception e) {System.out.println(e);}
		
	}
}

class Client {
	private String username;
	private String serverType = ""; // Largest server type
	private String currID = "0";
	private DataOutputStream dout;
	private BufferedReader in;
	// Last received message
	public String prevMessage = "";
	public boolean isAvailable = true; 
	
	// Constructor takes in output stream, reader and username
	public Client(DataOutputStream x, BufferedReader y, String uname) {
		dout = x;
		in = y;
		username = uname;
	}
	
	// sendOut sends a provided command to the server.
	public void sendOut(String command) {
		try {
			dout.write((command + "\n").getBytes());
			dout.flush();
			
			//System.out.println("OUT: " + command);
		}
		catch (Exception e) {}
	}
	
	// receiveIn receives a message from the server and stores it in prevMessage.
	public void receiveIn() {
		try {
			prevMessage = (String)in.readLine();
			
			//System.out.println("IN: " + prevMessage);
		}
		catch (Exception e) {}
	}
	
	// handshake follows the ds-sim protocol for initialising a connection between client and server.
	public void handshake() {
		try {
			sendOut("HELO");
			receiveIn();
			sendOut("AUTH " + username);
			receiveIn();
		}
		catch (Exception e) {}
	}
	
	// getResponseHeader takes in a server message and returns the header (e.g. JOBN, JCPL)
	public String getResponseHeader(String response) {
		String[] buffer = response.split(" ", 2);
		return buffer[0];
	}
	
	// scheduleJob schedules a job based on the jobID and the serverType.
	public void scheduleJob(String message) {
		try {
			String[] buffer = message.split(" ", 4);
			String jobID = buffer[2];
			sendOut("SCHD " + jobID + " " + serverType + " " + currID);
		}
		catch (Exception e) {}
	}

	// Find best fitting server using GETS Available. If no server exists, repeat with GETS Capable/RR to distribute.
	// Skip servers with same fitness value and look for lowest.
	// getFirstServer handles messaging the server to find the first capable server using GETS Capable
	// IN: core/memory/disk of job

	public void getFirstCapableServer(String cmd) {
		try {
			isAvailable = true;
			sendOut("GETS Capable" + " " + cmd);
			// DATA nServer
			receiveIn();
			String[] buffer = prevMessage.split(" ", 3);
			int serverNum = Integer.parseInt(buffer[1]);
			sendOut("OK");
			if (serverNum > 0) {
				// read first serverType
				receiveIn();
				buffer = prevMessage.split(" ", 6); 
				serverType = buffer[0];
				currID = buffer[1];
				for (int i = 1; i < serverNum; i++) 
					receiveIn();
			}	
			sendOut("OK");
			receiveIn();
		}
		catch (Exception e) {}
	}
	
	public void getFirstAvailServer(String cmd) {
		try {
			sendOut("GETS Avail" + " " + cmd);
			// DATA nServer
			receiveIn();	
			String[] buffer = prevMessage.split(" ", 3);
			int serverNum = Integer.parseInt(buffer[1]);
			sendOut("OK");
			
			if (serverNum > 0) {
				// read first serverType
				receiveIn();
				buffer = prevMessage.split(" ", 6); 
				serverType = buffer[0];
				currID = buffer[1];
				for (int i = 1; i < serverNum; i++) 
					receiveIn();
				sendOut("OK");
			}	
			else
				isAvailable = false;			
			receiveIn();
		}
		catch (Exception e) {}
	}

}
