import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client {
	public static void main(String[] args) {
		String jobID = ""; // ID
		String username = System.getProperty("user.name");
		Boolean hasServer = false;
		String serverType = ""; // Largest server type
		int currID = 0;
		int highestID = 0;
		int core = 0;
		try {
			Socket s = new Socket("localhost", 50000);
			System.out.println("Server connected.");
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			
			Client.sendSimpleCommand(dout, in, "HELO");
				 
			Client.sendSimpleCommand(dout, in, "AUTH " + username);
			
			
			jobID = Client.getJob(dout, in);
			System.out.println("jobID "+ jobID);
			
			
			dout.write(("GETS All\n").getBytes());
			dout.flush();
					dout.write(("OK\n").getBytes());
					dout.flush();
					String str = (String)in.readLine();
					// DATA nServer
					String[] buffer = str.split(" ", 3);
					int serverNum = Integer.parseInt(buffer[1]);
					dout.write(("OK\n").getBytes());
					dout.flush();
					// find server.
					for (int i = 0; i < serverNum; i++) {
						// find largest server type and largest index based on cores (serverType serverID status something core)
						str = (String)in.readLine();
						buffer = str.split(" ", 6); //todo
						// if buffer core > core than change serverType, set highest core, set highestID to 0	
						if (Integer.parseInt(buffer[4]) > core) {
							serverType = buffer[0];
							core = Integer.parseInt(buffer[4]);
							highestID = 0;
						}
						else if (buffer[0].equals(serverType)) {
							highestID++;
						}
						// if two types have identical cores, aim for the first serverType.
						System.out.println(str);
					} 
					System.out.println(highestID);
					System.out.println(serverType);
					
					dout.write(("OK\n").getBytes());
					dout.flush();
				
				
				
			/*while ((String)in.readLine() != "NONE") {
				// Send REDY	
				jobID = Client.getJob(dout, in);
				System.out.println("jobID "+ jobID);
				
				if (!hasServer) {
					dout.write(("GETS All\n").getBytes());
					dout.flush();
					String str = (String)in.readLine();	  
					System.out.println("Server message: " + str); 
				}
				
				//Client.sendSimpleCommand(dout, in, "SCHD " + jobID + " " + serverType + " 0");
				
				//Client.sendSimpleCommand(dout, in, "OK");
			}*/
			
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
		}
		catch (Exception e) {System.out.println(e);}
	}
	
	private static String getJob(DataOutputStream dout, BufferedReader in) {
		try {
			dout.write(("REDY\n").getBytes());
			dout.flush();
			String str = (String)in.readLine();
			
			System.out.println(str);
			// Need to get core memory disk from JOBN message	  
			String[] buffer = str.split(" ", 4);
			System.out.println("Buffer: " + buffer[2]);
			String jobID = buffer[2];
			
			return jobID;
		}
		catch (Exception e) {System.out.println(e); return "";}
	}
}
