import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.jcraft.jsch.JSchException;

public class Main {

	public static void main(String[] args) throws JSchException, IOException, InterruptedException {
		
		BufferedReader reader =  
                new BufferedReader(new InputStreamReader(System.in)); 
		
		System.out.println("Enter netid (azure username): "); 
        String user = reader.readLine(); 		
		
        AzureSSHClient client = new AzureSSHClient(user);
        
        client.startSideCar();
//        client.startAzureSSHClient();
	}
}
