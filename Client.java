import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client {
	public static void main(String[] args) {
		String jobID = "0";
		String serverType = ""; // Largest server type
		String jobInfo = ""; // Core Memory Disk
		String username = System.getProperty("user.name");
		try {
			Socket s = new Socket("localhost", 50000);
			System.out.println("Server connected.");
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			Client.sendSimpleCommand(dout, in, "HELO");
				 
			Client.sendSimpleCommand(dout, in, "AUTH " + username);
			
			// Send REDY	
			Client.getJob(dout, in, jobID, serverType, jobInfo);
			
			Client.getServer(dout, in, serverType);
			System.out.println("here");
			Client.sendSimpleCommand(dout, in, "SCHD " + jobID + " " + serverType + " 0");
			
			Client.sendSimpleCommand(dout, in, "OK");
			
			Client.sendSimpleCommand(dout, in, "QUIT");
			dout.close();
			s.close();
		}
		catch (Exception e) {System.out.println(e);}
		
	}
	private static void sendSimpleCommand(DataOutputStream dout, BufferedReader in, String command) {
		try {
			dout.write((command + "\n").getBytes());
			dout.flush();
			String str = (String)in.readLine();	  
			System.out.println("Server message: " + str); 
		}
		catch (Exception e) {System.out.println(e);}
	}
	
	private static void getJob(DataOutputStream dout, BufferedReader in, String jobID, String serverType, String jobInfo) {
		try {
			dout.write(("REDY\n").getBytes());
			dout.flush();
			String str = (String)in.readLine();	  
			str = str.substring(5);
			System.out.println("Server message: " + str); 
			int strip = str.indexOf(' ');
			String tempStr = str.substring(strip + 1);
			str = str.substring(strip + 1);
			strip = str.indexOf(' ');
			jobID = str.substring(0, strip + 1);
			
			tempStr = tempStr.substring(strip + 1);
			strip = tempStr.indexOf(' ');
			jobInfo = tempStr.substring(strip + 1);
			System.out.println(jobID); 
			dout.write(("OK\n").getBytes());
			dout.flush();
		}
		catch (Exception e) {System.out.println(e);}
	}
	
	private static void getServer(DataOutputStream dout, BufferedReader in, String serverType) {
		try {
			dout.write(("GETS All\n").getBytes());
			dout.flush();
			String str = (String)in.readLine();
			dout.write(("OK\n").getBytes());
			dout.flush();
			str = (String)in.readLine();
			while ((String)in.readLine() != null) {
				str = (String)in.readLine();
			}
			int strip = str.indexOf(' ');
			serverType = str.substring(0, strip + 1);
			dout.write(("OK\n").getBytes());
			dout.flush();
		}
		catch (Exception e) {System.out.println(e);}
	}
}
