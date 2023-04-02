import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Main {
	public static void main(String[] args) {
		//String jobID = ""; // ID
		String username = System.getProperty("user.name");
		String redyResponse = "";
		boolean hasServer = false;
		try {
			Socket s = new Socket("localhost", 50000);
			System.out.println("Server connected.");
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			Client c = new Client(dout, in, username);
			
			c.handshake();
			
			while (!(c.prevMessage.equals("NONE"))) {
				c.sendOut("REDY");
				c.receiveIn();
				// Store data for future reference.
				redyResponse = c.prevMessage;
				
				// Get server if largest server is not known.
				if (!hasServer) {
					c.getLargestServer();
					
					hasServer = true;
				}
				
				if (c.getResponseHeader(redyResponse).equals("JOBN")) {
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
	private int currID = 0;
	private int highestID = 0;
	private int core = 0;
	private DataOutputStream dout;
	private BufferedReader in;
	// Last received message
	public String prevMessage = "";
	
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
		}
		catch (Exception e) {}
	}
	
	// receiveIn receives a message from the server and stores it in prevMessage.
	public void receiveIn() {
		try {
			prevMessage = (String)in.readLine();
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
	
	// scheduleJob schedules a job based on the jobID and the current server ID currID.
	public void scheduleJob(String message) {
		try {
			String[] buffer = message.split(" ", 4);
			String jobID = buffer[2];
			// if currID less than or equal to highestID, schedule then increment currID
			// otherwise reset to 0 before scheduling.
			if (currID <= highestID) {
				sendOut("SCHD " + jobID + " " + serverType + " " + currID);
				currID++;
			}
			else {
				currID = 0;
				sendOut("SCHD " + jobID + " " + serverType + " " + currID);
				currID++;
			}
		}
		catch (Exception e) {}
	}
	
	// getLargestServer handles messaging the server for server data and returning the largest server available.
	public void getLargestServer() {
		try {
			sendOut("GETS All");
			// DATA nServer
			receiveIn();
			String[] buffer = prevMessage.split(" ", 3);
			int serverNum = Integer.parseInt(buffer[1]);
			sendOut("OK");
			// find largest server type and number of servers of the same type by looking for the largest number of cores.
			for (int i = 0; i < serverNum; i++) {
				receiveIn();
				buffer = prevMessage.split(" ", 6); 
				// if the current server type has more cores than the previous largest, match serverType and core to the current server type.
				if (Integer.parseInt(buffer[4]) > core) {
					serverType = buffer[0];
					core = Integer.parseInt(buffer[4]);
					highestID = 0;
				}
				// serverNum will only change for servers of the same type, even if two different types have identical number of cores.
				else if (buffer[0].equals(serverType)) {
					highestID++;
				}
			} 
			sendOut("OK");
			receiveIn();
		}
		catch (Exception e) {}
	}

}
