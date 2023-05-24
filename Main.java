import java.io.*;
import java.net.*;
import java.util.Scanner;
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
			
			// Change from FF to BF
			while (!(c.prevMessage.equals("NONE"))) {
				c.sendOut("REDY");
				c.receiveIn();
				// Store data for future reference. Split job data into core/memory/disk requirements
				redyResponse = c.prevMessage;
				if (c.getResponseHeader(redyResponse).equals("JOBN")) {
					String[] job = redyResponse.split(" ", 5);
					// Get the server to assign to. Pass in core/memory/disk
					c.getFirstServer(job[4]);
		

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
			sendOut("SCHD " + jobID + " " + serverType + " " + 0);
		}
		catch (Exception e) {}
	}

	// Find best fitting server using GETS Available. If no server exists, repeat with GETS Capable/RR to distribute.
	// Skip servers with same fitness value and look for lowest.
	// getFirstServer handles messaging the server to find the first capable server using GETS Capable
	// IN: core/memory/disk of job
	
	// MODIFY TO BF by using GETS Capable? to identify servers, find server with smallest fitness value.
	// Steps: On initial load, store maximum and current index of servers in 2D array. Load in job based on GETS Available. If none available,
	// use GETS Capable to find first best capable server of a given type. Find server, increment id by 1 until max, in that case revert to 0. (VERY SLOW ON LARGE NUMBER OF SERVERS)
	// use java dictionary with server type as key, value is tupule [current index, max size]
	public void getFirstServer(String cmd) {
		try {
			sendOut("GETS Capable" + " " + cmd);
			// DATA nServer
			receiveIn();
			String[] buffer = prevMessage.split(" ", 3);
			int serverNum = Integer.parseInt(buffer[1]);
			sendOut("OK");
			// read first serverType
			receiveIn();
			if (serverNum > 0) {
				buffer = prevMessage.split(" ", 6); 
				serverType = buffer[0];
				for (int i = 1; i < serverNum; i++) 
					receiveIn();
			}		
			sendOut("OK");
			receiveIn();
		}
		catch (Exception e) {}
	}

}
