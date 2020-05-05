import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import redis.clients.jedis.Jedis;

import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class AzureSSHClient {
    private static final String HAO_HOST_1 = "40.121.57.154";
    private static final String HAO_HOST_2 = "40.121.233.18";
    private static final String FRANK_HOST = "13.92.44.67";
    private static final String REDIS_HOST = "40.87.94.171";
    
    private static final String HAO_PRIVATE_KEY = "/Users/haoli/.ssh/id_rsa";
    private static final String FRANK_PRIVATE_KEY = "/Users/frankrodriguez/.ssh/id_rsa";
    
    private static final String command1 = "free -m | awk \'NR==2{printf \"Memory Usage: %s/%sMB (%.2f%%)\", $3,$2,$3*100/$2 }\' ";
    private static final String command2 = "df -h | awk \'$NF==\"/\"{printf \"Disk Usage: %d/%dGB (%s)\", $3,$2,$5}\'";
    private static final String command3 = "awk -v a=\"$(awk \'/cpu /{print $2+$4,$2+$4+$5}\' /proc/stat; sleep 1)\" \'/cpu /{split(a,b,\" \"); print 100*($2+$4-b[1])/($2+$4+$5-b[2])}\'  /proc/stat";
    
    private List<String> servers;
    private List<String> commands;
    private String azureUsername;
    private String privateKey;
    private Jedis jedis;
    
    public AzureSSHClient(String user) {
    	this.azureUsername = user;
    	this.jedis = new Jedis(REDIS_HOST);
    	
    	if (user.equals("hl677")) {
    		this.servers = Arrays.asList(HAO_HOST_1, HAO_HOST_2);
    		this.privateKey = HAO_PRIVATE_KEY;
    	}
    	else if (user.equals("fsr32")) {
    		this.servers = Arrays.asList(FRANK_HOST);
    		this.privateKey = FRANK_PRIVATE_KEY;
    	}
    	
    	this.commands = Arrays.asList(command1, command2, command3);
    }
    
    public void startSideCar() throws JSchException, IOException, InterruptedException {
    	Session session = setupSshSession(FRANK_HOST);
    	boolean flag = true;
    	session.connect();
    	String c1 = "git clone https://github.com/haoli98/Cloud-Diagnostics-Tool.git; cd Cloud-Diagnostics-Tool; mkdir hello; ls";
    	
        Channel channel=session.openChannel("exec");
    	((ChannelExec)channel).setCommand(c1);
        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);
        
        InputStream in=channel.getInputStream();
        channel.connect();
        
        byte[] tmp=new byte[1024];
        while(flag){
          while(in.available()>0){
            int i=in.read(tmp, 0, 1024);
            if(i<=0)break;
            
            String data = new String(tmp, 0, i);
            this.jedis.publish(this.azureUsername, data);
            System.out.println(data);
            flag = false;
          }
        }
        flag = true;
        channel.disconnect();
        session.disconnect();
    }
    
    
    public void startAzureSSHClient() throws JSchException, IOException, InterruptedException {
    	System.out.println("Starting azure client for user: " + this.azureUsername);
    	
    	while (true) {
    		for (String server: this.servers) {
    			this.runCommand(server);	
    		}
    		
    		// Collect metrics every 5 seconds
    		TimeUnit.SECONDS.sleep(5);
    	}
    }

    private void runCommand(String server) throws JSchException, IOException {
        Session session = setupSshSession(server);
        session.connect();

        boolean flag = true;
        for (String command : this.commands) { 
            Channel channel=session.openChannel("exec");
            
        	((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            
            InputStream in=channel.getInputStream();
            channel.connect();
            
            byte[] tmp=new byte[1024];
            while(flag){
              while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<=0)break;
                
                String data = new String(tmp, 0, i);
                this.jedis.publish(this.azureUsername, data);
                System.out.println(data);
                flag = false;
              }
            }
            flag = true;
            channel.disconnect();
    }
        session.disconnect();

    }
    private Session setupSshSession(String server) throws JSchException {
    	try {
            JSch jsch=new JSch();
      		jsch.addIdentity(this.privateKey);

      		String host = this.azureUsername + "@" + server;
      		
      		String user=host.substring(0, host.indexOf('@'));
      		host=host.substring(host.indexOf('@')+1);
      		
      		Session session=jsch.getSession(user, host, 22);
      		session.setConfig("StrictHostKeyChecking", "no"); // disable check for RSA key
      		return session;
      		
        } catch(Exception e){
          System.out.println(e);
          return null;
        }
      }
}