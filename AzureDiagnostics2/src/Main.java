import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.jcraft.jsch.JSchException;

public class Main {

	public static void main(String[] args) throws JSchException, IOException, InterruptedException {
		BufferedReader reader =  
                new BufferedReader(new InputStreamReader(System.in)); 
		
		System.out.println("Do you want to add an ID or server to metric collection? (yes/no)"); 
        String addUser = reader.readLine(); 
        
        if (addUser.equals("yes")) {
    		System.out.println("Enter username:"); 
            String userName = reader.readLine(); 	
            
            // Make directory
            String path = "../../users/" + userName;
            File f = new File(path);
            if (f.mkdirs()) {
            	System.out.println("created directory at: " + "/home/fsr32/src/users/" + userName);
            } else {
            	System.out.println("directory already exists");
            }
           
            
            System.out.println("Do you want to add a server for this username? (yes/no)");
            String userServer = reader.readLine();
            
            while (userServer.equals("yes")) {
            	System.out.println("What host address do you want to add?");
            	String hostAddress = reader.readLine();
            	
            	// Save host address somewhere
                try {
                	// First create hosts.txt if doesn't exist
                	FileWriter myWriter = new FileWriter(path + "/hosts.txt"); 
                    myWriter.write(hostAddress);
                    myWriter.close();
                    
                    // Create private keys directory if doesn't exist
                    String privateKeyDirectoryPath = path + "/keys";
                    new File(privateKeyDirectoryPath).mkdirs();
                    
                    // Create file for private key
                    String privateKeyFilePath = privateKeyDirectoryPath + "/" + hostAddress + ".txt";
                    File myObj = new File(privateKeyFilePath);
                    
                    if (myObj.createNewFile()) {
                      String linuxAddress = "/home/fsr32/src/users/" + userName + "/keys/" + hostAddress + ".txt";
                      System.out.println("File created for VM: " + hostAddress + ". Add private key to file at: " + linuxAddress);
                    } else {
                      System.out.println("File already exists.");
                    }
                    
                    
                  } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                  }    	
                System.out.println("Do you want to add another server for this username? (yes/no)");
                userServer = reader.readLine();
            		
            }
        }
        
        // Get all users
        File[] files = new File("../../users/").listFiles();
        List<String> users = new ArrayList<String>();
        
        System.out.println("Running program for usernames: ");
        for (File file : files) {
            if (file.isDirectory()) {
            	String userID = file.getName();
            	System.out.println(userID);
            	users.add(file.getName());
            }
        }
        
        // Get servers for each user
        HashMap<String, List<String>> id_server_map = new HashMap<>();
        for (String user : users) {
        	List<String> servers = new ArrayList<String>();
        	File hosts = new File("../../users/" +  user + "/hosts.txt"); 
        	
            Scanner myReader = new Scanner(hosts);
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
              servers.add(data);
              System.out.println(data);
            }
        	
            id_server_map.put(user, servers);
            myReader.close();
        }
        
         	
        AzureSSHClient client = new AzureSSHClient(users, id_server_map);
        client.startAzureSSHClient();
	}
}
